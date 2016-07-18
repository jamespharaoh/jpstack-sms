package wbs.sms.message.outbox.daemon;

import static wbs.framework.utils.etc.Misc.isPresent;
import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

import com.google.common.base.Optional;

import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.record.GlobalId;
import wbs.platform.daemon.AbstractDaemonService;
import wbs.sms.message.core.logic.MessageLogic;
import wbs.sms.message.outbox.logic.OutboxLogic;
import wbs.sms.message.outbox.logic.OutboxLogic.FailureType;
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
@Log4j
public abstract
class AbstractSmsSender1<MessageContainer>
	extends AbstractDaemonService {

	private final static
	int maxTries = 10;

	private final static
	int retryTimeMs = 10;

	private final static
	int waitTimeMs = 1000;

	// dependencies

	@Inject
	BlacklistObjectHelper blacklistHelper;

	@Inject
	Database database;

	@Inject
	MessageLogic messageLogic;

	@Inject
	NumberLookupManager numberLookupManager;

	@Inject
	OutboxLogic outboxLogic;

	@Inject
	SmsOutboxMonitor outboxMonitor;

	@Inject
	RouteObjectHelper routeHelper;

	@Inject
	SenderObjectHelper senderHelper;

	// properties

	@Getter @Setter
	int threadsPerRoute = 4;

	protected abstract
	String getSenderCode ();

	protected abstract
	MessageContainer getMessage (
			OutboxRec outbox)
		throws SendFailureException;

	protected abstract
	Object sendMessage (
			MessageContainer message)
		throws SendFailureException;

	@Override
	protected
	void createThreads () {

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				"AbstractSmsSender1.createThreads ()",
				this);

		// get a list of routes

		SenderRec sender =
			senderHelper.findByCodeRequired (
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
						threadName +
						route.getId () +
						(char) ((int) 'a' + i));

				thread.start ();

				registerThread (thread);

			}

		}

	}

	/**
	 * One per worker thread, knows which route to watch and will wait for
	 * traffic on it, then keep sending messages until there are no more.
	 */
	class Worker
		implements Runnable {

		int routeId;

		Object routeLock;

		Worker (
				int newRouteId,
				Object newRouteLock) {

			routeId = newRouteId;
			routeLock = newRouteLock;

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

			Thread.sleep (waitTimeMs);

			outboxMonitor.waitForRoute (routeId);

		}

		void processMessages () {

			while (! Thread.interrupted ()) {

				// get the next message from the database, synchronize to stop
				// deadlocks because all threads go for the
				// same message

				OutboxRec outbox;

				int messageId = -1; // always reinitialised

				MessageContainer message;

				synchronized (routeLock) {

					@Cleanup
					Transaction transaction =
						database.beginReadWrite (
							"AbstractSmsSender1.Worker.processMessages ()",
							this);

					RouteRec route =
						routeHelper.findRequired (
							routeId);

					// get the next message

					outbox =
						outboxLogic.claimNextMessage (
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

						messageLogic.blackListMessage (
							outbox.getMessage ());

						transaction.commit ();

						continue;

					}

					// TODO aaargh

					Optional<BlacklistRec> blacklistOptional =
						blacklistHelper.findByCode (
							GlobalId.root,
							number);

					if (
						isPresent (
							blacklistOptional)
					) {

						outbox.setSending (null);

						messageLogic.blackListMessage (
							outbox.getMessage ());

						transaction.commit ();

						continue;

					}

					// run the getMessage hook also

					try {

						message =
							getMessage (outbox);

					} catch (SendFailureException exception) {

						outboxLogic.messageFailure (
							messageId,
							exception.errorMessage,
							exception.failureType);

						transaction.commit ();

						continue;

					}

					transaction.commit ();

				}

				// now send it

				Object otherIdObject;

				try {

					otherIdObject =
						sendMessage (message);

				} catch (SendFailureException exception) {

					reliableOutboxFailure (
						messageId,
						exception);

					continue;

				}

				String[] otherIds;

				if (otherIdObject == null) {

					otherIds = null;

				} else if (otherIdObject instanceof String) {

					otherIds =
						new String [] {
							(String) otherIdObject
						};

				} else if (otherIdObject instanceof String[]) {

					otherIds =
						(String[])
						otherIdObject;

				} else {

					throw new RuntimeException (
						otherIdObject.getClass ().toString ());

				}

				// and save our success

				reliableOutboxSuccess (
					messageId,
					otherIds);

			}

		}

		/**
		 * Calls smsUtils.outboxSuccess in a transaction. Will automatically
		 * retry up to 100 times in case of a DataAcccessException being thrown.
		 */
		void reliableOutboxSuccess (
				int messageId,
				String[] otherIds) {

			boolean interrupted = false;

			for (int tries = 0;;) {

				try {

					@Cleanup
					Transaction transaction =
						database.beginReadWrite (
							"AbstractSmsSender1.Worker.reliableOutboxSuccess (...)",
							this);

					outboxLogic.messageSuccess (
						messageId,
						otherIds);

					if (interrupted)
						Thread.currentThread ().interrupt ();

					transaction.commit ();

					return;

				} catch (Exception updateException) {

					if (++ tries == maxTries) {

						log.fatal (
							stringFormat (
								"Outbox success for message %s failed %s ",
								messageId,
								maxTries,
								"times, giving up"),
							updateException);

					}

					log.warn (
						stringFormat (
							"Outbox success for message %s failed, retrying",
							messageId),
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
				int messageId,
				SendFailureException sendException) {

			boolean interrupted = false;

			for (int tries = 0;;) {

				try {

					@Cleanup
					Transaction transaction =
						database.beginReadWrite (
							"AbstractSmsSender1.Worker.reliableOutboxFailure (...)",
							this);

					outboxLogic.messageFailure (
						messageId,
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
			FailureType.perm);

	}

	public static
	SendFailureException tempFailure (
			String errorMessage) {

		return new SendFailureException (
			errorMessage,
			FailureType.temp);

	}

	public static
	SendFailureException dailyFailure (
			String errorMessage) {

		return new SendFailureException (
			errorMessage,
			FailureType.daily);

	}

}
