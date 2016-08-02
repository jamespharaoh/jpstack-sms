package wbs.sms.message.outbox.daemon;

import static wbs.framework.utils.etc.JsonUtils.jsonToBytes;
import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.ifNull;
import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.Misc.isPresent;
import static wbs.framework.utils.etc.Misc.joinWithSeparator;
import static wbs.framework.utils.etc.Misc.notEqual;
import static wbs.framework.utils.etc.Misc.stringFormat;
import static wbs.framework.utils.etc.Misc.todo;

import java.util.stream.LongStream;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.Duration;
import org.json.simple.JSONObject;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.ExceptionUtils;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.record.GlobalId;
import wbs.sms.message.core.logic.MessageLogic;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.daemon.SmsSenderHelper.PerformSendResult;
import wbs.sms.message.outbox.daemon.SmsSenderHelper.PerformSendStatus;
import wbs.sms.message.outbox.daemon.SmsSenderHelper.ProcessResponseResult;
import wbs.sms.message.outbox.daemon.SmsSenderHelper.ProcessResponseStatus;
import wbs.sms.message.outbox.daemon.SmsSenderHelper.SetupRequestResult;
import wbs.sms.message.outbox.daemon.SmsSenderHelper.SetupRequestStatus;
import wbs.sms.message.outbox.logic.SmsOutboxLogic;
import wbs.sms.message.outbox.logic.SmsOutboxLogic.FailureType;
import wbs.sms.message.outbox.model.OutboxObjectHelper;
import wbs.sms.message.outbox.model.OutboxRec;
import wbs.sms.message.outbox.model.SmsOutboxAttemptObjectHelper;
import wbs.sms.message.outbox.model.SmsOutboxAttemptRec;
import wbs.sms.number.blacklist.model.BlacklistObjectHelper;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.number.lookup.logic.NumberLookupManager;
import wbs.sms.route.core.model.RouteRec;

@Accessors (fluent = true)
@PrototypeComponent ("genericSmsSender")
public
class GenericSmsSenderImplementation<StateType>
	implements GenericSmsSender {

	// constants

	final
	long databaseRetriesMax = 1000;

	final
	Duration databaseRetryWaitMax =
		Duration.standardSeconds (1);

	// dependencies

	@Inject
	Database database;

	@Inject
	ExceptionLogger exceptionLogger;

	@Inject
	ExceptionUtils exceptionUtils;

	@Inject
	NumberLookupManager numberLookupManager;

	@Inject
	BlacklistObjectHelper smsBlacklistHelper;

	@Inject
	MessageLogic smsMessageLogic;

	@Inject
	SmsOutboxAttemptObjectHelper smsOutboxAttemptHelper;

	@Inject
	OutboxObjectHelper smsOutboxHelper;

	@Inject
	SmsOutboxLogic smsOutboxLogic;

	// properties

	@Getter
	SmsSenderHelper<StateType> smsSenderHelper;

	@Override
	public
	GenericSmsSenderImplementation<StateType> smsSenderHelper (
			@NonNull SmsSenderHelper<?> smsSenderHelper) {

		@SuppressWarnings ("unchecked")
		SmsSenderHelper<StateType> smsSenderHelperTemp =
			(SmsSenderHelper<StateType>)
			smsSenderHelper;

		this.smsSenderHelper =
			smsSenderHelperTemp;

		return this;

	}

	@Getter @Setter
	Long smsMessageId;

	// state

	StateType state;

	Long smsOutboxAttemptId;

	SetupRequestResult<StateType> setupRequestResult;
	PerformSendResult performSendResult;
	ProcessResponseResult processResponseResult;

	// implementation

	@Override
	public
	void send () {

System.out.println ("XXX sender send 0");

		// setup request

		setupSend ();

System.out.println ("XXX sender send 1");

		if (
			notEqual (
				setupRequestResult.status (),
				SetupRequestStatus.success)
		) {

System.out.println ("XXX sender send 2");

			return;

		}

System.out.println ("XXX sender send 3");

		state =
			setupRequestResult.state ();

System.out.println ("XXX sender send 4");

		// perform send

System.out.println ("XXX sender send 5");

		performSend ();

System.out.println ("XXX sender send 6");

		if (
			notEqual (
				performSendResult.status (),
				PerformSendStatus.success)
		) {

System.out.println ("XXX sender send 7");

			handlePerformSendError ();

System.out.println ("XXX sender send 8");

			return;

		}

		// process response

System.out.println ("XXX sender send 9");

		processResponse ();

System.out.println ("XXX sender send 10");

	}

	void setupSend () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"GenericSmsSenderImplementation.setupSend ()",
				this);

		OutboxRec smsOutbox =
			smsOutboxHelper.findRequired (
				smsMessageId);

		MessageRec smsMessage =
			smsOutbox.getMessage ();

		RouteRec smsRoute =
			smsOutbox.getRoute ();

		// check block number list

		if (
			checkBlacklist (
				smsRoute,
				smsMessage.getNumber ())
		) {

			smsOutbox

				.setSending (
					null);

			smsMessageLogic.blackListMessage (
				smsOutbox.getMessage ());

			transaction.commit ();

			setupRequestResult =
				new SetupRequestResult<StateType> ()

				.status (
					SetupRequestStatus.blacklisted)

				.statusMessage (
					"Not sending message due to blacklist");

			return;

		}

		// call setup send hook

		try {

			setupRequestResult =
				smsSenderHelper.setupRequest (
					smsOutbox);

			if (

				isNull (
					setupRequestResult)

				|| isNull (
					setupRequestResult.status ())

			) {
				throw new NullPointerException ();
			}

		} catch (Exception exception) {

			exceptionLogger.logThrowable (
				"console",
				getClass ().getSimpleName (),
				exception,
				Optional.absent (),
				GenericExceptionResolution.fatalError);

			setupRequestResult =
				new SetupRequestResult<StateType> ()

				.status (
					SetupRequestStatus.unknownError)

				.statusMessage (
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
				setupRequestResult.status (),
				SetupRequestStatus.success)
		) {

			smsOutboxLogic.messageFailure (
				smsMessage,
				setupRequestResult.statusMessage (),
				SmsOutboxLogic.FailureType.permanent);

			transaction.commit ();

			return;

		}

		// create send attempt

		SmsOutboxAttemptRec smsOutboxAttempt =
			smsOutboxLogic.beginSendAttempt (
				smsOutbox,
				jsonToBytes (
					setupRequestResult.requestTrace ()));

		smsOutboxAttemptId =
			(long) smsOutboxAttempt.getId ();

		// commit and return

		transaction.commit ();

	}

	void performSend () {

		// now send it

		try {

			performSendResult =
				smsSenderHelper.performSend (
					state);

			if (

				isNull (
					performSendResult)

				|| isNull (
					performSendResult.status ())

			) {
				throw new NullPointerException ();
			}

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

				isNotNull (
					performSendResult.exception ())

				&& isNull (
					performSendResult.errorTrace ())

			) {

				performSendResult.errorTrace (
					exceptionUtils.throwableDumpJson (
						performSendResult.exception ()));

			}

			if (

				isNotNull (
					performSendResult.exception ())

				&& isNull (
					performSendResult.statusMessage ())

			) {

				performSendResult.statusMessage (
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

			if (
				isNull (
					performSendResult.errorTrace ())
			) {

				performSendResult.errorTrace (
					new JSONObject ());

			}

		}

	}

	void handlePerformSendError () {

		boolean success =
			LongStream.of (databaseRetriesMax).anyMatch (
				attemptNumber ->
					attemptToHandlePerformSendError (
						attemptNumber));

		if (! success) {

			throw todo ();

		}

	}

	boolean attemptToHandlePerformSendError (
			@NonNull Long attemptNumber) {

		try {

			attemptToHandlePerformSendErrorReal (
				attemptNumber);

			return true;

		} catch (Throwable exception) {

			exceptionLogger.logThrowable (
				"daemon",
				getClass ().getSimpleName (),
				exception,
				Optional.absent (),
				GenericExceptionResolution.tryAgainLater);

			return false;

		}

	}

	void attemptToHandlePerformSendErrorReal (
			@NonNull Long attemptNumber) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				stringFormat (
					"%s.%s (%s)",
					"GenericSmsSenderImplementation",
					"attemptToHandlePerformSendErrorReal",
					joinWithSeparator (
						", ",
						stringFormat (
							"smsMessageId = %s",
							smsMessageId),
						stringFormat (
							"smsOutboxAttemptId = %s",
							smsOutboxAttemptId),
						stringFormat (
							"attemptNUmber = %s",
							attemptNumber))),
				this);

		SmsOutboxAttemptRec smsOutboxAttempt =
			smsOutboxAttemptHelper.findRequired (
				smsOutboxAttemptId);

		smsOutboxLogic.completeSendAttemptFailure (
			smsOutboxAttempt,
			FailureType.temporary,
			performSendResult.statusMessage (),
			Optional.absent (),
			jsonToBytes (
				performSendResult.errorTrace ()));

		transaction.commit ();

	}

	void processResponse () {

		boolean success =
			LongStream.of (databaseRetriesMax).anyMatch (
				attemptNumber ->
					attemptToProcessResponse (
						state,
						attemptNumber));

		if (! success) {

			throw todo ();

		}

	}

	boolean attemptToProcessResponse (
			@NonNull StateType state,
			@NonNull Long attemptNumber) {

		try {

			attemptToProcessResponseReal (
				state,
				attemptNumber);

			return true;

		} catch (Throwable exception) {

			exceptionLogger.logThrowable (
				"daemon",
				getClass ().getSimpleName (),
				exception,
				Optional.absent (),
				GenericExceptionResolution.tryAgainLater);

			return false;

		}

	}

	void attemptToProcessResponseReal (
			@NonNull StateType state,
			@NonNull Long attemptNumber) {

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				stringFormat (
					"%s.%s (%s)",
					"GenericSmsSenderImplementation",
					"attemptToProcessResponseReal",
					joinWithSeparator (
						", ",
						stringFormat (
							"smsMessageId = %s",
							smsMessageId),
						stringFormat (
							"smsOutboxAttemptId = %s",
							smsOutboxAttemptId),
						stringFormat (
							"attemptNUmber = %s",
							attemptNumber))),
				this);

		SmsOutboxAttemptRec smsOutboxAttempt =
			smsOutboxAttemptHelper.findRequired (
				smsOutboxAttemptId);

		// process response

		try {

			processResponseResult =
				smsSenderHelper.processSend (
					state);

			if (

				isNull (
					processResponseResult)

				|| isNull (
					processResponseResult.status ())

			) {
				throw new NullPointerException ();
			}

		} catch (Exception exception) {

			processResponseResult =
				new ProcessResponseResult ()

				.status (
					ProcessResponseStatus.unknownError)

				.exception (
					exception);

		}

		// store result

		if (
			equal (
				processResponseResult.status (),
				ProcessResponseStatus.success)
		) {

			smsOutboxLogic.completeSendAttemptSuccess (
				smsOutboxAttempt,
				Optional.fromNullable (
					processResponseResult.otherIds ()),
				jsonToBytes (
					performSendResult.responseTrace ()));

		} else {

			smsOutboxLogic.completeSendAttemptFailure (
				smsOutboxAttempt,
				processResponseResult.failureType (),
				processResponseResult.statusMessage (),
				Optional.of (
					jsonToBytes (
						performSendResult.responseTrace ())),
				jsonToBytes (
					processResponseResult.errorTrace ()));

		}

		// commit and return

		transaction.commit ();

	}

	boolean checkBlacklist (
			@NonNull RouteRec smsRoute,
			@NonNull NumberRec number) {

		// check route block list

		String numberString =
			number.getNumber ();

		if (

			isNotNull (
				smsRoute.getBlockNumberLookup ())

			&& numberLookupManager.lookupNumber (
				smsRoute.getBlockNumberLookup (),
				number)

		) {
			return true;
		}

		// check global blacklist

		// TODO aaargh

		if (
			isPresent (
				smsBlacklistHelper.findByCode (
					GlobalId.root,
					numberString))
		) {
			return true;
		}

		// not blacklisted

		return false;

	}

}
