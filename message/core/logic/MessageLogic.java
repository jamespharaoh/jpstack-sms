package wbs.sms.message.core.logic;

import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;

public
interface MessageLogic {

	// TODO move this
	boolean isChatMessage (
			MessageRec message);

	void messageStatus (
			MessageRec message,
			MessageStatus newStatus);

	void blackListMessage (
			MessageRec message);

}
