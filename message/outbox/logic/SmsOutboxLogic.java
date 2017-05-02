package wbs.sms.message.outbox.logic;

import java.util.List;

import com.google.common.base.Optional;

import wbs.framework.database.Transaction;

import wbs.platform.text.model.TextRec;

import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageTypeRec;
import wbs.sms.message.outbox.model.OutboxRec;
import wbs.sms.message.outbox.model.SmsOutboxAttemptRec;
import wbs.sms.route.core.model.RouteRec;

public
interface SmsOutboxLogic {

	MessageRec resendMessage (
			Transaction parentTransaction,
			MessageRec old,
			RouteRec route,
			Optional <TextRec> newText,
			Optional <MessageTypeRec> newMessageType);

	void unholdMessage (
			Transaction parentTransaction,
			MessageRec message);

	void cancelMessage (
			Transaction parentTransaction,
			MessageRec message);

	OutboxRec claimNextMessage (
			Transaction parentTransaction,
			RouteRec route);

	List <OutboxRec> claimNextMessages (
			Transaction parentTransaction,
			RouteRec route,
			Long limit);

	void messageSuccess (
			Transaction parentTransaction,
			MessageRec message,
			Optional <List <String>> otherIds,
			Optional <Long> simulateMultipart);

	void messageFailure (
			Transaction parentTransaction,
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
			Transaction parentTransaction,
			MessageRec message);

	SmsOutboxAttemptRec beginSendAttempt (
			Transaction parentTransaction,
			OutboxRec smsOutbox,
			Optional <byte[]> requestTrace);

	void completeSendAttemptSuccess (
			Transaction parentTransaction,
			SmsOutboxAttemptRec smsOutboxAttempt,
			Optional <List <String>> otherIds,
			Optional <Long> simulateMultipart,
			Optional <byte[]> requestTrace,
			Optional <byte[]> responseTrace);

	void completeSendAttemptFailure (
			Transaction parentTransaction,
			SmsOutboxAttemptRec smsOutboxAttempt,
			FailureType failureType,
			String errorMessage,
			Optional <byte[]> requestTrace,
			Optional <byte[]> responseTrace,
			Optional <byte[]> errorTrace);

}
