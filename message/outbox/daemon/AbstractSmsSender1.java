package wbs.sms.message.outbox.daemon;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.string.StringUtils.joinWithoutSeparator;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.daemon.AbstractDaemonService;

import wbs.sms.message.core.logic.SmsMessageLogic;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.logic.SmsOutboxLogic;
import wbs.sms.message.outbox.logic.SmsOutboxLogic.FailureType;
import wbs.sms.message.outbox.model.OutboxRec;
import wbs.sms.number.blacklist.model.BlacklistObjectHelper;
import wbs.sms.number.blacklist.model.BlacklistRec;
import wbs.sms.number.lookup.logic.NumberLookupManager;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;
import wbs.sms.route.sender.model.SenderObjectHelper;
import wbs.sms.route.sender.model.SenderRec;

/**
 * Abstract DaemonService for sender daemons which process each message
 * individually.
 *
 * Generic type M represents the information retrieved from the database and
 * used by the subclasses sendMessage method. In the simplest case this can be
 * just Outbox, and getMessage (..) can simply return the same value it is
 * passed. More commonly extra route information will need to be looked up and
 * combined into a custom class.
 */
@Deprecated
@Log4j
public abstract
class AbstractSmsSender1 <MessageContainer>
	extends AbstractDaemonService {

	private final static
	int maxTries = 10;

	private final static
	int retryTimeMs = 10;

	private final static
	int waitTimeMs = 1000;

	// singleton dependencies

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	NumberLookupManager numberLookupManager;

	@SingletonDependency
	BlacklistObjectHelper smsBlacklistHelper;

	@SingletonDependency
	MessageObjectHelper smsMessageHelper;

	@SingletonDependency
	SmsMessageLogic smsMessageLogic;

	@SingletonDependency
	SmsOutboxLogic smsOutboxLogic;

	@SingletonDependency
	SmsOutboxMonitor smsOutboxMonitor;

	@SingletonDependency
	RouteObjectHelper smsRouteHelper;

	@SingletonDependency
	SenderObjectHelper smsSenderHelper;

	// properties

	@Getter @Setter
	int threadsPerRoute = 4;

	protected abstract
	String getSenderCode ();

	protected abstract
	MessageContainer getMessage (
			TaskLogger parentTaskLogger,
			OutboxRec smsOutbox)
		throws SendFailureException;

	protected abstract
	Optional <List <String>> sendMessage (
			TaskLogger parentTaskLogger,
			MessageContainer messageContainer)
		throws SendFailureException;

	@Override
	protected
	void createThreads (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"createThreads");

			OwnedTransaction transaction =
				database.beginReadOnly (
					taskLogger,
					"AbstractSmsSender1.createThreads ()",
					this);

		) {

			// get a list of routes

			SenderRec sender =
				smsSenderHelper.findByCodeRequired (
					GlobalId.root,
					getSenderCode ());

			// and for each one...

			String threadName =
				getThreadName ();

			for (
				RouteRec route
					: sender.getRoutes ()
			) {

				Object routeLock =
					new Object ();

				for (int i = 0; i < threadsPerRoute; i++) {

					Worker worker =
						new Worker (
							route.getId (),
							routeLock);

					Thread thread =
						threadManager.makeThread (worker);

					if (threadName != null)

						thread.setName (
							joinWithoutSeparator (
								threadName,
								Long.toString (
									route.getId ()),
								new String (
									Character.toChars (
										'a' + i))));

					thread.start ();

					registerThread (
						thread);

				}

			}

		}

	}

	/**
	 * One per worker thread, knows which route to watch and will wait for
	 * traffic on it, then keep sending messages until there are no more.
	 */
	class Worker
		implements Runnable {

		Long routeId;

		Object routeLock;

		Worker (
				@NonNull Long newRouteId,
				@NonNull Object newRouteLock) {

			routeId =
				newRouteId;

			routeLock =
				newRouteLock;

		}

		@Override
		public
		void run () {

			try {

				while (true) {

					waitForMessages ();

					processMessages ();

				}

			} catch (InterruptedException exception) {
				return;
			}

		}

		private
		void waitForMessages ()
			throws InterruptedException {

			Thread.sleep (
				waitTimeMs);

			smsOutboxMonitor.waitForRoute (
				routeId);

		}

		void processMessages () {

			try (

				TaskLogger taskLogger =
					logContext.createTaskLogger (
						"processMessages");

			) {

				while (! Thread.interrupted ()) {

					// get the next message from the database, synchronize to stop
					// deadlocks because all threads go for the
					// same message

					OutboxRec outbox;

					long messageId = -1; // always reinitialised

					MessageContainer messageContainer;

					synchronized (routeLock) {

						try (

							OwnedTransaction transaction =
								database.beginReadWrite (
									taskLogger,
									"AbstractSmsSender1.Worker.processMessages ()",
									this);

						) {

							RouteRec route =
								smsRouteHelper.findRequired (
									routeId);

							// get the next message

							outbox =
								smsOutboxLogic.claimNextMessage (
									taskLogger,
									route);

							if (outbox == null) {

								return;

							}

							messageId =
								outbox.getMessage ().getId ();

							String number =
								outbox
									.getMessage ()
									.getNumber ()
									.getNumber ();

							// check block number list

							// TODO right place for this..?

							if (

								route.getBlockNumberLookup () != null

								&& numberLookupManager.lookupNumber (
									route.getBlockNumberLookup (),
									outbox.getMessage ().getNumber ())

							) {

								outbox.setSending (null);

								smsMessageLogic.blackListMessage (
									taskLogger,
									outbox.getMessage ());

								transaction.commit ();

								continue;

							}

							// TODO aaargh

							Optional<BlacklistRec> blacklistOptional =
								smsBlacklistHelper.findByCode (
									GlobalId.root,
									number);

							if (
								optionalIsPresent (
									blacklistOptional)
							) {

								outbox.setSending (null);

								smsMessageLogic.blackListMessage (
									taskLogger,
									outbox.getMessage ());

								transaction.commit ();

								continue;

							}

							// run the getMessage hook also

							try {

								messageContainer =
									getMessage (
										taskLogger,
										outbox);

							} catch (SendFailureException exception) {

								smsOutboxLogic.messageFailure (
									taskLogger,
									outbox.getMessage (),
									exception.errorMessage,
									exception.failureType);

								transaction.commit ();

								continue;

							}

							transaction.commit ();

						}

					}

					// now send it

					Optional<List<String>> otherIds;

					try {

						otherIds =
							sendMessage (
								taskLogger,
								messageContainer);

					} catch (SendFailureException exception) {

						reliableOutboxFailure (
							taskLogger,
							messageId,
							exception);

						continue;

					}

					// and save our success

					reliableOutboxSuccess (
						taskLogger,
						messageId,
						otherIds);

				}

			}

		}

		/**
		 * Calls smsUtils.outboxSuccess in a transaction. Will automatically
		 * retry up to 100 times in case of a DataAcccessException being thrown.
		 */
		void reliableOutboxSuccess (
				@NonNull TaskLogger parentTaskLogger,
				@NonNull Long messageId,
				@NonNull Optional <List <String>> otherIds) {

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"reliableOutboxSuccess");

			boolean interrupted = false;

			for (int tries = 0;;) {

				try (

					OwnedTransaction transaction =
						database.beginReadWrite (
							taskLogger,
							"AbstractSmsSender1.Worker.reliableOutboxSuccess (...)",
							this);

				) {

					MessageRec message =
						smsMessageHelper.findRequired (
							messageId);

					smsOutboxLogic.messageSuccess (
						taskLogger,
						message,
						otherIds,
						optionalAbsent ());

					if (interrupted)
						Thread.currentThread ().interrupt ();

					transaction.commit ();

					return;

				} catch (Exception updateException) {

					if (++ tries == maxTries) {

						log.fatal (
							stringFormat (
								"Outbox success for message %s failed %s ",
								integerToDecimalString (
									messageId),
								integerToDecimalString (
									maxTries),
								"times, giving up"),
							updateException);

					}

					log.warn (
						stringFormat (
							"Outbox success for message %s failed, retrying",
							integerToDecimalString (
								messageId)),
						updateException);

					try {

						Thread.sleep (
							retryTimeMs);

					} catch (InterruptedException interruptException) {

						interrupted = true;

					}

				}

			}

		}

		/**
		 * Calls smsUtils.outboxFailure in a transaction. Will automatically
		 * retry up to 100 times in case of a DataAcccessException being thrown.
		 */
		void reliableOutboxFailure (
				@NonNull TaskLogger parentTaskLogger,
				long messageId,
				SendFailureException sendException) {

			try (

				TaskLogger taskLogger =
					logContext.nestTaskLogger (
						parentTaskLogger,
						"reliableOutboxFailure");

			) {

				boolean interrupted = false;

				for (int tries = 0;;) {

					try (

						OwnedTransaction transaction =
							database.beginReadWrite (
								taskLogger,
								"AbstractSmsSender1.Worker.reliableOutboxFailure (...)",
								this);

					) {

						MessageRec message =
							smsMessageHelper.findRequired (
								messageId);

						smsOutboxLogic.messageFailure (
							taskLogger,
							message,
							sendException.errorMessage,
							sendException.failureType);

						if (interrupted)
							Thread.currentThread ().interrupt ();

						transaction.commit ();

						return;

					} catch (Exception updateException) {

						if (++tries == maxTries) {

							log.fatal (
								"Outbox failure for message " + messageId + " " +
								"failed many times, giving up!");

							throw new RuntimeException (
								"Max tries exceeded",
								updateException);

						}

						log.warn (
							"Outbox failure for message " + messageId + " " +
							"failed, retrying...",
							updateException);

						try {

							Thread.sleep (retryTimeMs);

						} catch (InterruptedException interruptException) {

							interrupted = true;

						}

					}

				}

			}

		}

	} // private class Worker

	public static
	class SendFailureException
		extends RuntimeException {

		private static final
		long serialVersionUID =
			-3183837427723031874L;

		String errorMessage;
		FailureType failureType;

		public
		SendFailureException (
				String newErrorMessage,
				FailureType newFailureType) {

			errorMessage = newErrorMessage;
			failureType = newFailureType;

		}

	}

	public static
	SendFailureException permFailure (
			String errorMessage) {

		return new SendFailureException (
			errorMessage,
			FailureType.permanent);

	}

	public static
	SendFailureException tempFailure (
			String errorMessage) {

		return new SendFailureException (
			errorMessage,
			FailureType.temporary);

	}

	public static
	SendFailureException dailyFailure (
			String errorMessage) {

		return new SendFailureException (
			errorMessage,
			FailureType.daily);

	}

}
