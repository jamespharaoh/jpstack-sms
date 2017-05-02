package wbs.sms.message.core.logic;

import com.google.common.base.Optional;

import wbs.framework.database.Transaction;

import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;

public
interface SmsMessageLogic {

	// TODO move this
	boolean isChatMessage (
			Transaction parentTransaction,
			MessageRec message);

	void messageStatus (
			Transaction parentTransaction,
			MessageRec message,
			MessageStatus newStatus);

	// TODO move this
	void blackListMessage (
			Transaction parentTransaction,
			MessageRec message);

	String mangleMessageId (
			Transaction parentTransaction,
			Long messageId);

	Optional <Long> unmangleMessageId (
			Transaction parentTransaction,
			String mangledMessageId);

	Optional <MessageRec> findMessageByMangledId (
			Transaction parentTransaction,
			String mangledMessageId);

}
