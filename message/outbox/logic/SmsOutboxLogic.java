package wbs.sms.message.outbox.logic;

import java.util.List;

import com.google.common.base.Optional;

import wbs.framework.logging.TaskLogger;

import wbs.platform.text.model.TextRec;

import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageTypeRec;
import wbs.sms.message.outbox.model.OutboxRec;
import wbs.sms.message.outbox.model.SmsOutboxAttemptRec;
import wbs.sms.route.core.model.RouteRec;

public
interface SmsOutboxLogic {

	MessageRec resendMessage (
			TaskLogger parentTaskLogger,
			MessageRec old,
			RouteRec route,
			TextRec textRec,
			MessageTypeRec msgTypeRec);

	void unholdMessage (
			TaskLogger parentTaskLogger,
			MessageRec message);

	void cancelMessage (
			TaskLogger parentTaskLogger,
			MessageRec message);

	OutboxRec claimNextMessage (
			RouteRec route);

	List <OutboxRec> claimNextMessages (
			RouteRec route,
			Long limit);

	void messageSuccess (
			TaskLogger parentTaskLogger,
			MessageRec message,
			Optional <List <String>> otherIds,
			Optional <Long> simulateMultipart);

	void messageFailure (
			TaskLogger parentTaskLogger,
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
			TaskLogger parentTaskLogger,
			MessageRec message);

	SmsOutboxAttemptRec beginSendAttempt (
			TaskLogger parentTaskLogger,
			OutboxRec smsOutbox,
			Optional <byte[]> requestTrace);

	void completeSendAttemptSuccess (
			TaskLogger parentTaskLogger,
			SmsOutboxAttemptRec smsOutboxAttempt,
			Optional <List <String>> otherIds,
			Optional <Long> simulateMultipart,
			Optional <byte[]> requestTrace,
			Optional <byte[]> responseTrace);

	void completeSendAttemptFailure (
			TaskLogger parentTaskLogger,
			SmsOutboxAttemptRec smsOutboxAttempt,
			FailureType failureType,
			String errorMessage,
			Optional <byte[]> requestTrace,
			Optional <byte[]> responseTrace,
			Optional <byte[]> errorTrace);

}
