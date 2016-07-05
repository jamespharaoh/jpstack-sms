package wbs.sms.message.outbox.daemon;

import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.isPresent;
import static wbs.framework.utils.etc.Misc.notEqual;
import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.stringToBytes;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;

import org.json.simple.JSONObject;

import com.google.common.base.Optional;

import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.record.GlobalId;
import wbs.platform.daemon.AbstractDaemonService;
import wbs.sms.message.core.logic.MessageLogic;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.logic.OutboxLogic;
import wbs.sms.message.outbox.model.OutboxRec;
import wbs.sms.message.outbox.model.SmsOutboxAttemptObjectHelper;
import wbs.sms.message.outbox.model.SmsOutboxAttemptRec;
import wbs.sms.message.outbox.model.SmsOutboxAttemptState;
import wbs.sms.number.blacklist.model.BlacklistObjectHelper;
import wbs.sms.number.blacklist.model.BlacklistRec;
import wbs.sms.number.lookup.logic.NumberLookupManager;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;
import wbs.sms.route.sender.model.SenderObjectHelper;
import wbs.sms.route.sender.model.SenderRec;

@Log4j
public abstract
class AbstractSmsSender2
	extends AbstractDaemonService {

	// dependencies

	@Inject
	BlacklistObjectHelper blacklistHelper;

	@Inject
	Database database;

	@Inject
	ExceptionLogger exceptionLogger;

	@Inject
	MessageLogic messageLogic;

	@Inject
	NumberLookupManager numberLookupManager;

	@Inject
	OutboxLogic outboxLogic;

	@Inject
	SmsOutboxMonitor outboxMonitor;

	@Inject
	SmsOutboxAttemptObjectHelper smsOutboxAttemptHelper;

	@Inject
	RouteObjectHelper routeHelper;

	@Inject
	SenderObjectHelper senderHelper;

	// properties

	@Getter @Setter
	int maxTries = 10;

	@Getter @Setter
	int retryTimeMs = 10;

	@Getter @Setter
	int waitTimeMs = 1000;

	@Getter @Setter
	int threadsPerRoute = 4;

	// extension points

	public abstract
	String senderCode ();

	protected abstract
	SetupSendResult setupSend (
			OutboxRec outbox);

	// implementation

	@Override
	protected
	void createThreads () {

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				this);

		// get a list of routes

		SenderRec sender =
			senderHelper.findByCodeRequired (
				GlobalId.root,
				senderCode ());

		Set<RouteRec> routes =
			sender.getRoutes ();

		// and for each one...

		String threadName =
			getThreadName ();

		for (RouteRec route : routes) {

			Object routeLock =
				new Object ();

			for (
				int index = 0;
				index < threadsPerRoute;
				index ++
			) {

				Worker worker =
					new Worker (
						route.getId (),
						routeLock);

				Thread thread =
					threadManager.makeThread (
						worker);

				if (threadName != null)

					thread.setName (
						threadName +
						route.getId () +
						(char) ((int) 'a' + index));

				thread.start ();

				registerThread (
					thread);

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

			routeId =
				newRouteId;

			routeLock =
				newRouteLock;

		}

		@Override
		public
		void run () {

			try {

				for (;;) {

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

			outboxMonitor.waitForRoute (
				routeId);

		}

		void processMessages () {

			while (! Thread.interrupted ()) {

				boolean messageProcessed =
					processOneMessage ();

				if (! messageProcessed)
					return;

			}

		}

		boolean processOneMessage () {

			OutboxRec outbox;
			int messageId;
			int smsOutboxAttemptId;
			SetupSendResult setupSendResult;

			synchronized (routeLock) {

				@Cleanup
				Transaction transaction =
					database.beginReadWrite (
						this);

				RouteRec route =
					routeHelper.findOrNull (
						routeId);

				// get the next message

				outbox =
					outboxLogic.claimNextMessage (
						route);

				if (outbox == null) {

					return false;

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

					return true;

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

					return true;

				}

				// call setup send hook

				try {

					setupSendResult =
						setupSend (
							outbox);

					if (setupSendResult == null)
						throw new NullPointerException ();

					if (setupSendResult.status () == null)
						throw new NullPointerException ();

				} catch (Exception exception) {

					exceptionLogger.logThrowable (
						"console",
						getClass ().getSimpleName (),
						exception,
						Optional.absent (),
						GenericExceptionResolution.fatalError);

					setupSendResult =
						new SetupSendResult ()

						.status (
							SetupSendStatus.unknownError)

						.message (
							stringFormat (
								"Error setting up send: %s: %s",
								exception.getClass ().getSimpleName (),
								ifNull (
									exception.getMessage (),
									"null")))

						.exception (
							exception);

				}

				// handle error

				if (
					notEqual (
						setupSendResult.status (),
						SetupSendStatus.success)
				) {

					outboxLogic.messageFailure (
						messageId,
						setupSendResult.message (),
						OutboxLogic.FailureType.perm);

					transaction.commit ();

					return true;

				}

				// create send attempt

				MessageRec message =
					outbox.getMessage ();

				SmsOutboxAttemptRec smsOutboxAttempt =
					smsOutboxAttemptHelper.insert (
						smsOutboxAttemptHelper.createInstance ()

					.setMessage (
						message)

					.setIndex (
						(int) (long)
						message.getNumAttempts ())

					.setState (
						SmsOutboxAttemptState.sending)

					.setRoute (
						outbox.getRoute ())

					.setStartTime (
						transaction.now ())

					.setRequestTrace (
						stringToBytes (
							setupSendResult
								.requestTrace ()
								.toJSONString (),
							"utf-8"))

				);

				smsOutboxAttemptId =
					smsOutboxAttempt.getId ();

				message

					.setNumAttempts (
						message.getNumAttempts () + 1);

				// commit

				transaction.commit ();

			}

			// now send it

			PerformSendResult performSendResult;

			try {

				performSendResult =
					setupSendResult.performSend ().call ();

				if (performSendResult == null)
					throw new NullPointerException ();

				if (performSendResult.status () == null)
					throw new NullPointerException ();

			} catch (Exception exception) {

				performSendResult =
					new PerformSendResult ()

					.status (
						PerformSendStatus.unknownError)

					.exception (
						exception);

			}

			if (
				notEqual (
					performSendResult.status (),
					PerformSendStatus.success)
			) {

				if (
					performSendResult.exception () == null
					&& performSendResult.message () == null
				) {

					performSendResult.message (
						stringFormat (
							"Error sending message: %s: %s",
							performSendResult
								.exception ()
								.getClass ()
								.getSimpleName (),
							performSendResult
								.exception ()
								.getMessage ()));
				}

				reliableOutboxFailure (
					messageId,
					smsOutboxAttemptId,
					performSendResult.message (),
					OutboxLogic.FailureType.temp,
					performSendResult.responseTrace (),
					performSendResult.errorTrace ());

				return true;

			}

			// and save our success

			reliableOutboxSuccess (
				messageId,
				smsOutboxAttemptId,
				performSendResult.otherIds (),
				performSendResult.responseTrace ());

			return true;

		}

		/**
		 * Calls smsUtils.outboxSuccess in a transaction. Will automatically
		 * retry up to 100 times in case of a DataAcccessException being thrown.
		 */
		void reliableOutboxSuccess (
				int messageId,
				int smsOutboxAttemptId,
				List<String> otherIds,
				JSONObject responseTrace) {

			boolean interrupted = false;

			for (int tries = 0;;) {

				try {

					@Cleanup
					Transaction transaction =
						database.beginReadWrite (
							this);

					outboxLogic.messageSuccess (
						messageId,
						otherIds.toArray (
							new String [] {}));

					SmsOutboxAttemptRec smsOutboxAttempt =
						smsOutboxAttemptHelper.findOrNull (
							smsOutboxAttemptId);

					smsOutboxAttempt

						.setState (
							SmsOutboxAttemptState.success)

						.setEndTime (
							transaction.now ())

						.setResponseTrace (
							responseTrace != null
								? stringToBytes (
									responseTrace.toJSONString (),
									"utf-8")
								: null);

					transaction.commit ();

					if (interrupted)
						Thread.currentThread ().interrupt ();

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
				int smsOutboxAttemptId,
				String errorMessage,
				OutboxLogic.FailureType failureType,
				JSONObject responseTrace,
				JSONObject errorTrace) {

			boolean interrupted = false;

			int tries = 0;

			for (;;) {

				try {

					@Cleanup
					Transaction transaction =
						database.beginReadWrite (
							this);

					outboxLogic.messageFailure (
						messageId,
						errorMessage,
						failureType);

					SmsOutboxAttemptRec smsOutboxAttempt =
						smsOutboxAttemptHelper.findOrNull (
							smsOutboxAttemptId);

					smsOutboxAttempt

						.setState (
							SmsOutboxAttemptState.failure)

						.setEndTime (
							transaction.now ())

						.setResponseTrace (
							responseTrace != null
							? stringToBytes (
								responseTrace.toJSONString (),
								"utf-8")
							: null)

						.setErrorTrace (
							errorTrace != null
							? stringToBytes (
								errorTrace.toJSONString (),
								"utf-8")
							: null);

					transaction.commit ();

					if (interrupted)
						Thread.currentThread ().interrupt ();

					return;

				} catch (Exception updateException) {

					if (++ tries == maxTries) {

						log.fatal (
							stringFormat (
								"Outbox failure for message %s failed %s ",
								messageId,
								tries,
								"times, giving up"));

						throw new RuntimeException (
							"Max tries exceeded",
							updateException);

					}

					log.warn (
						stringFormat (
							"Outbox failure for message %s failed, retrying",
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

	}

	// data structures

	@Accessors (fluent = true)
	@Data
	public static
	class SetupSendResult {
		SetupSendStatus status;
		String message;
		JSONObject requestTrace;
		Throwable exception;
		Callable<PerformSendResult> performSend;
	}

	public static
	enum SetupSendStatus {
		success,
		configError,
		unknownError,
		validationError;
	}

	@Accessors (fluent = true)
	@Data
	public static
	class PerformSendResult {
		PerformSendStatus status;
		String message;
		Throwable exception;
		List<String> otherIds;
		JSONObject responseTrace;
		JSONObject errorTrace;
	}

	public static
	enum PerformSendStatus {
		success,
		localError,
		communicationError,
		remoteError,
		unknownError;
	}

}
