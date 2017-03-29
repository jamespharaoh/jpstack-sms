package wbs.sms.message.core.logic;

import com.google.common.base.Optional;

import wbs.framework.logging.TaskLogger;

import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;

public
interface SmsMessageLogic {

	// TODO move this
	boolean isChatMessage (
			MessageRec message);

	void messageStatus (
			TaskLogger parentTaskLogger,
			MessageRec message,
			MessageStatus newStatus);

	// TODO move this
	void blackListMessage (
			TaskLogger parentTaskLogger,
			MessageRec message);

	String mangleMessageId (
			Long messageId);

	Optional <Long> unmangleMessageId (
			String mangledMessageId);

	Optional <MessageRec> findMessageByMangledId (
			String mangledMessageId);

}
