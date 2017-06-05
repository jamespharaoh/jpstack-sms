package wbs.sms.message.outbox.daemon;

import static wbs.utils.etc.EnumUtils.enumEqualSafe;
import static wbs.utils.etc.EnumUtils.enumNotEqualSafe;
import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.Misc.todo;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalMapRequired;
import static wbs.utils.string.StringUtils.keyEqualsDecimalInteger;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Map;
import java.util.stream.LongStream;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.Duration;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.ExceptionUtils;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.sms.message.core.logic.SmsMessageLogic;
import wbs.sms.message.core.model.MessageObjectHelper;
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

import wbs.web.utils.JsonUtils;

@Accessors (fluent = true)
@PrototypeComponent ("genericSmsSender")
public
class GenericSmsSenderImplementation <StateType>
	implements GenericSmsSender {

	// constants

	final
	long databaseRetriesMax = 1000;

	final
	Duration databaseRetryWaitMax =
		Duration.standardSeconds (1);

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	ExceptionLogger exceptionLogger;

	@SingletonDependency
	ExceptionUtils exceptionUtils;

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
	SmsOutboxAttemptObjectHelper smsOutboxAttemptHelper;

	@SingletonDependency
	OutboxObjectHelper smsOutboxHelper;

	@SingletonDependency
	SmsOutboxLogic smsOutboxLogic;

	// properties

	@Getter
	SmsSenderHelper <StateType> smsSenderHelper;

	@Override
	public
	GenericSmsSenderImplementation <StateType> smsSenderHelper (
			@NonNull SmsSenderHelper <?> smsSenderHelper) {

		@SuppressWarnings ("unchecked")
		SmsSenderHelper <StateType> smsSenderHelperTemp =
			(SmsSenderHelper <StateType>)
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

	SetupRequestResult <StateType> setupRequestResult;
	PerformSendResult performSendResult;
	ProcessResponseResult processResponseResult;

	// implementation

	@Override
	public
	void send (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"send");

		) {

			// setup request

			setupSend (
				taskLogger);

			if (
				enumNotEqualSafe (
					setupRequestResult.status (),
					SetupRequestStatus.success)
			) {

				return;

			}

			state =
				setupRequestResult.state ();

			// perform send

			performSend (
				taskLogger);

			if (
				enumNotEqualSafe (
					performSendResult.status (),
					PerformSendStatus.success)
			) {

				handlePerformSendError (
					taskLogger);

				return;

			}

			// process response

			processResponse (
				taskLogger);

		}

	}

	void setupSend (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"setupSend");

		) {

			OutboxRec smsOutbox =
				smsOutboxHelper.findRequired (
					transaction,
					smsMessageId);

			MessageRec smsMessage =
				smsOutbox.getMessage ();

			RouteRec smsRoute =
				smsOutbox.getRoute ();

			// check block number list

			if (
				checkBlacklist (
					transaction,
					smsRoute,
					smsMessage.getNumber ())
			) {

				smsOutbox

					.setSending (
						null);

				smsMessageLogic.blackListMessage (
					transaction,
					smsOutbox.getMessage ());

				transaction.commit ();

				setupRequestResult =
					new SetupRequestResult <StateType> ()

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
						transaction,
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
					transaction,
					"daemon",
					getClass ().getSimpleName (),
					exception,
					optionalAbsent (),
					GenericExceptionResolution.tryAgainLater);

				setupRequestResult =
					new SetupRequestResult <StateType> ()

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
				enumNotEqualSafe (
					setupRequestResult.status (),
					SetupRequestStatus.success)
			) {

				try {

					smsOutboxLogic.messageFailure (
						transaction,
						smsMessage,
						setupRequestResult.statusMessage (),
						ifThenElse (
							enumEqualSafe (
								setupRequestResult.status (),
								SetupRequestStatus.validationError),
							() -> SmsOutboxLogic.FailureType.permanent,
							() -> SmsOutboxLogic.FailureType.temporary));

					transaction.commit ();

				} catch (RuntimeException exception) {

					exceptionLogger.logThrowable (
						transaction,
						"daemon",
						getClass ().getSimpleName (),
						exception,
						optionalAbsent (),
						GenericExceptionResolution.tryAgainNow);

					transaction.close ();

					handleSetupErrorInSeparateTransaction (
						transaction,
						smsMessage.getId (),
						setupRequestResult);

				}

				return;

			}

			// create send attempt

			SmsOutboxAttemptRec smsOutboxAttempt =
				smsOutboxLogic.beginSendAttempt (
					transaction,
					smsOutbox,
					optionalMapRequired (
						optionalFromNullable (
							setupRequestResult.requestTrace ()),
						JsonUtils::jsonToBytes));

			smsOutboxAttemptId =
				smsOutboxAttempt.getId ();

			// commit and return

			transaction.commit ();

		}

	}

	void handleSetupErrorInSeparateTransaction (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long smsMessageId,
			@NonNull SetupRequestResult <StateType> setupSendResult) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"handleSetupErrorInSeparateTransaction");

		) {

			MessageRec smsMessage =
				smsMessageHelper.findRequired (
					transaction,
					smsMessageId);

			smsOutboxLogic.messageFailure (
				transaction,
				smsMessage,
				setupRequestResult.statusMessage (),
				ifThenElse (
					enumEqualSafe (
						setupRequestResult.status (),
						SetupRequestStatus.validationError),
					() -> SmsOutboxLogic.FailureType.permanent,
					() -> SmsOutboxLogic.FailureType.temporary));

			transaction.commit ();

		}

	}

	private
	void performSend (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"performSend");

		) {

			// now send it

			try {

				performSendResult =
					smsSenderHelper.performSend (
						taskLogger,
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
				enumNotEqualSafe (
					performSendResult.status (),
					PerformSendStatus.success)
			) {

				// set status message from exception if not present

				if (

					isNotNull (
						performSendResult.exception ())

					&& isNull (
						performSendResult.statusMessage ())

				) {

					Throwable exception =
						performSendResult.exception ();

					performSendResult.statusMessage (
						ifThenElse (
							isNotNull (
								exception.getMessage ()),

						() -> stringFormat (
							"Error sending message: %s: %s",
							exception.getClass ().getSimpleName (),
							exception.getMessage ()),

						() -> stringFormat (
							"Error sending message: %s",
							exception.getClass ().getSimpleName ())

					));

				}

				// set error trace from exception if not present

				if (

					isNotNull (
						performSendResult.exception)

					&& isNull (
						performSendResult.errorTrace ())

				) {

					performSendResult.errorTrace (
						exceptionUtils.throwableDumpJson (
							taskLogger,
							performSendResult.exception ()));

				}

			}

		}

	}

	void handlePerformSendError (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"handlePerformSendError");

		) {

			boolean success =
				LongStream.range (0, databaseRetriesMax).anyMatch (
					attemptNumber ->
						attemptToHandlePerformSendError (
							taskLogger,
							attemptNumber));

			if (! success) {
				throw todo ();
			}

		}

	}

	boolean attemptToHandlePerformSendError (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long attemptNumber) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"attemptToHandlePerformSendError");

		) {

			try {

				attemptToHandlePerformSendErrorReal (
					taskLogger,
					attemptNumber);

				return true;

			} catch (Throwable exception) {

				exceptionLogger.logThrowable (
					taskLogger,
					"daemon",
					getClass ().getSimpleName (),
					exception,
					optionalAbsent (),
					GenericExceptionResolution.tryAgainLater);

				return false;

			}

		}

	}

	void attemptToHandlePerformSendErrorReal (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long attemptNumber) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"attemptToHandlePerformSendErrorReal",
					keyEqualsDecimalInteger (
						"smsMessageId",
						smsMessageId),
					keyEqualsDecimalInteger (
						"smsOutboxAttemptId",
						smsOutboxAttemptId),
					keyEqualsDecimalInteger (
						"attemptNUmber",
						attemptNumber));

		) {

			SmsOutboxAttemptRec smsOutboxAttempt =
				smsOutboxAttemptHelper.findRequired (
					transaction,
					smsOutboxAttemptId);

			smsOutboxLogic.completeSendAttemptFailure (
				transaction,
				smsOutboxAttempt,
				FailureType.temporary,
				performSendResult.statusMessage (),
				optionalMapRequired (
					Optional.fromNullable (
						performSendResult.requestTrace ()),
					JsonUtils::jsonToBytes),
				optionalMapRequired (
					Optional.fromNullable (
						performSendResult.responseTrace ()),
					JsonUtils::jsonToBytes),
				optionalMapRequired (
					Optional.fromNullable (
						performSendResult.errorTrace ()),
					JsonUtils::jsonToBytes));

			transaction.commit ();

		}

	}

	void processResponse (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"processResponse");

		) {

			boolean success =
				LongStream.range (0, databaseRetriesMax).anyMatch (
					attemptNumber ->
						attemptToProcessResponse (
							taskLogger,
							state,
							attemptNumber));

			if (! success) {
				throw todo ();
			}

		}

	}

	boolean attemptToProcessResponse (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull StateType state,
			@NonNull Long attemptNumber) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"attemptToProcessResponse");

		) {

			try {

				attemptToProcessResponseReal (
					taskLogger,
					state,
					attemptNumber);

				return true;

			} catch (Throwable exception) {

				exceptionLogger.logThrowable (
					taskLogger,
					"daemon",
					getClass ().getSimpleName (),
					exception,
					optionalAbsent (),
					GenericExceptionResolution.tryAgainLater);

				return false;

			}

		}

	}

	void attemptToProcessResponseReal (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull StateType state,
			@NonNull Long attemptNumber) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"attemptToProcessResponseReal",
					keyEqualsDecimalInteger (
						"smsMessageId",
						smsMessageId),
					keyEqualsDecimalInteger (
						"smsOutboxAttemptId",
						smsOutboxAttemptId),
					keyEqualsDecimalInteger (
						"attemptNumber",
						attemptNumber));

		) {

			SmsOutboxAttemptRec smsOutboxAttempt =
				smsOutboxAttemptHelper.findRequired (
					transaction,
					smsOutboxAttemptId);

			// process response

			try {

				processResponseResult =
					smsSenderHelper.processSend (
						transaction,
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
						exception)

					.failureType (
						FailureType.temporary);

			}

			if (
				enumNotEqualSafe (
					processResponseResult.status (),
					ProcessResponseStatus.success)
			) {

				// set status message from exception if not present

				if (

					isNotNull (
						processResponseResult.exception ())

					&& isNull (
						processResponseResult.statusMessage ())

				) {

					Throwable exception =
						processResponseResult.exception ();

					processResponseResult.statusMessage (
						ifThenElse (
							isNotNull (
								exception.getMessage ()),

						() -> stringFormat (
							"Error sending message: %s: %s",
							exception.getClass ().getSimpleName (),
							exception.getMessage ()),

						() -> stringFormat (
							"Error sending message: %s",
							exception.getClass ().getSimpleName ())

					));

				}

				// set error trace from exception if not present

				if (

					isNotNull (
						processResponseResult.exception)

					&& isNull (
						processResponseResult.errorTrace ())

				) {

					processResponseResult.errorTrace (
						exceptionUtils.throwableDumpJson (
							transaction,
							processResponseResult.exception ()));

				}

			}

			// store result

			if (
				enumEqualSafe (
					processResponseResult.status (),
					ProcessResponseStatus.success)
			) {

				smsOutboxLogic.completeSendAttemptSuccess (
					transaction,
					smsOutboxAttempt,
					optionalFromNullable (
						processResponseResult.otherIds ()),
					optionalFromNullable (
						processResponseResult.simulateMessageParts ()),
					optionalMapRequired (
						optionalFromNullable (
							performSendResult.requestTrace ()),
						JsonUtils::jsonToBytes),
					optionalMapRequired (
						optionalFromNullable (
							performSendResult.responseTrace ()),
						JsonUtils::jsonToBytes));

			} else {

				if (
					isNull (
						processResponseResult.failureType ())
				) {

					transaction.warningFormat (
						"No failure type for send attempt %s ",
						integerToDecimalString (
							smsOutboxAttemptId),
						"(defaulting to temporary)");

					processResponseResult.failureType (
						FailureType.temporary);

				}

				if (
					isNull (
						processResponseResult.statusMessage ())
				) {

					processResponseResult.statusMessage (
						defaultStatusMessages.get (
							processResponseResult.failureType ()));

				}

				smsOutboxLogic.completeSendAttemptFailure (
					transaction,
					smsOutboxAttempt,
					processResponseResult.failureType (),
					processResponseResult.statusMessage (),
					optionalMapRequired (
						Optional.fromNullable (
							performSendResult.requestTrace ()),
						JsonUtils::jsonToBytes),
					optionalMapRequired (
						Optional.fromNullable (
							performSendResult.responseTrace ()),
						JsonUtils::jsonToBytes),
					optionalMapRequired (
						Optional.fromNullable (
							processResponseResult.errorTrace ()),
						JsonUtils::jsonToBytes));

			}

			// commit and return

			transaction.commit ();

		}

	}

	boolean checkBlacklist (
			@NonNull Transaction parentTransaction,
			@NonNull RouteRec smsRoute,
			@NonNull NumberRec number) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"checkBlacklist");

		) {

			// check route block list

			String numberString =
				number.getNumber ();

			if (

				isNotNull (
					smsRoute.getBlockNumberLookup ())

				&& numberLookupManager.lookupNumber (
					transaction,
					smsRoute.getBlockNumberLookup (),
					number)

			) {
				return true;
			}

			// check global blacklist

			// TODO aaargh

			if (
				optionalIsPresent (
					smsBlacklistHelper.findByCode (
						transaction,
						GlobalId.root,
						numberString))
			) {
				return true;
			}

			// not blacklisted

			return false;

		}

	}

	public final static
	Map <FailureType, String> defaultStatusMessages =
		ImmutableMap.<FailureType, String> builder ()

		.put (
			FailureType.daily,
			"Unspecified daily failure")

		.put (
			FailureType.permanent,
			"Unspecified permanent failure")

		.put (
			FailureType.temporary,
			"Unspecified temporart failure")

		.build ();

}
