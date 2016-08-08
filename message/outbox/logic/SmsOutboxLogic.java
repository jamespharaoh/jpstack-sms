package wbs.sms.message.outbox.logic;

import java.util.List;

import com.google.common.base.Optional;

import wbs.platform.text.model.TextRec;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageTypeRec;
import wbs.sms.message.outbox.model.OutboxRec;
import wbs.sms.message.outbox.model.SmsOutboxAttemptRec;
import wbs.sms.route.core.model.RouteRec;

public
interface SmsOutboxLogic {

	MessageRec resendMessage (
			MessageRec old,
			RouteRec route,
			TextRec textRec,
			MessageTypeRec msgTypeRec);

	void unholdMessage (
			MessageRec message);

	/**
	 * Removes the given message from the outbox and marks it as cancelled.
	 *
	 * @param message
	 */
	void cancelMessage (
			MessageRec message);

	/**
	 * Finds the next pending message on the given route and marks it as
	 * sending.
	 *
	 * @param routeId
	 * @return
	 */
	OutboxRec claimNextMessage (
			RouteRec route);

	List<OutboxRec> claimNextMessages (
			RouteRec route,
			int limit);

	/**
	 * Removes the given message from the outbox and marks it as sent
	 * successfully. Also associates the given otherId with the message. The
	 * message must already be marked as sending.
	 *
	 * @param messageId
	 * @param otherId
	 */
	void messageSuccess (
			MessageRec message,
			Optional<List<String>> otherIds);

	/**
	 * Removes the given message from the outbox and marks it as failed. The
	 * message must be marked as sending.
	 *
	 * @param messageId
	 * @param error
	 * @param failureType
	 */
	void messageFailure (
			MessageRec message,
			String error,
			FailureType failureType);

	public static
	enum FailureType {
		permanent,
		temporary,
		daily
	}

	void retryMessage (
			MessageRec message);

	SmsOutboxAttemptRec beginSendAttempt (
			OutboxRec smsOutbox,
			Optional<byte[]> requestTrace);

	void completeSendAttemptSuccess (
			SmsOutboxAttemptRec smsOutboxAttempt,
			Optional<List<String>> otherIds,
			Optional<byte[]> requestTrace,
			Optional<byte[]> responseTrace);

	void completeSendAttemptFailure (
			SmsOutboxAttemptRec smsOutboxAttempt,
			FailureType failureType,
			String errorMessage,
			Optional<byte[]> requestTrace,
			Optional<byte[]> responseTrace,
			Optional<byte[]> errorTrace);

}
