package wbs.sms.message.outbox.daemon;

import static wbs.utils.string.StringUtils.stringFormatArray;

import java.util.List;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import org.json.simple.JSONObject;

import wbs.framework.database.Transaction;
import wbs.framework.logging.TaskLogger;

import wbs.sms.message.outbox.logic.SmsOutboxLogic.FailureType;
import wbs.sms.message.outbox.model.OutboxRec;

public
interface SmsSenderHelper <StateType> {

	// methods

	String senderCode ();

	SetupRequestResult <StateType> setupRequest (
			Transaction parentTransaction,
			OutboxRec outbox);

	PerformSendResult performSend (
			TaskLogger taskLogger,
			StateType state);

	ProcessResponseResult processSend (
			Transaction parentTransaction,
			StateType state);

	// data structures

	@Accessors (fluent = true)
	@Data
	public static
	class SetupRequestResult <StateType> {

		SetupRequestStatus status;
		String statusMessage;

		Throwable exception;

		StateType state;

		JSONObject requestTrace;
		JSONObject errorTrace;

		// property utils

		public
		SetupRequestResult <StateType> statusMessageFormat (
				@NonNull String ... arguments) {

			return statusMessage (
				stringFormatArray (
					arguments));

		}

	}

	public static
	enum SetupRequestStatus {
		success,
		configError,
		unknownError,
		validationError,
		blacklisted;
	}

	@Accessors (fluent = true)
	@Data
	public static
	class PerformSendResult {

		PerformSendStatus status;
		String statusMessage;
		Throwable exception;

		JSONObject requestTrace;
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

	@Accessors (fluent = true)
	@Data
	public static
	class ProcessResponseResult {

		ProcessResponseStatus status;
		String statusMessage;

		Throwable exception;
		FailureType failureType;

		List <String> otherIds;
		Long simulateMessageParts;

		JSONObject errorTrace;

		// property utils

		public
		ProcessResponseResult statusMessageFormat (
				@NonNull String ... arguments) {

			return statusMessage (
				stringFormatArray (
					arguments));

		}

	}

	public static
	enum ProcessResponseStatus {
		success,
		localError,
		remoteError,
		unknownError;
	}

}
