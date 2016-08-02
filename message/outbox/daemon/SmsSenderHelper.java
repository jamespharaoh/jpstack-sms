package wbs.sms.message.outbox.daemon;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import org.json.simple.JSONObject;

import wbs.sms.message.outbox.logic.SmsOutboxLogic.FailureType;
import wbs.sms.message.outbox.model.OutboxRec;

public
interface SmsSenderHelper<StateType> {

	// methods

	String senderCode ();

	SetupRequestResult<StateType> setupRequest (
			OutboxRec outbox);

	PerformSendResult performSend (
			StateType state);

	ProcessResponseResult processSend (
			StateType state);

	// data structures

	@Accessors (fluent = true)
	@Data
	public static
	class SetupRequestResult<StateType> {

		SetupRequestStatus status;
		String statusMessage;

		Throwable exception;

		StateType state;

		JSONObject requestTrace;
		JSONObject errorTrace;

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

		List<String> otherIds;

		JSONObject errorTrace;

	}

	public static
	enum ProcessResponseStatus {
		success,
		localError,
		remoteError,
		unknownError;
	}

}
