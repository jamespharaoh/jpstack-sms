package wbs.sms.number.core.logic;

import wbs.framework.logging.TaskLogger;

import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.number.core.model.NumberRec;

public
interface NumberLogic {

	// TODO wtf?
	@Deprecated
	void updateDeliveryStatusForNumber (
			TaskLogger taskLogger,
			String numTo,
			MessageStatus status);

	@Deprecated
	NumberRec archiveNumberFromMessage (
			TaskLogger taskLogger,
			MessageRec message);

}
