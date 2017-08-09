package wbs.sms.number.core.logic;

import wbs.framework.database.Transaction;

import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.number.core.model.NumberRec;

public
interface NumberLogic {

	// TODO wtf?
	@Deprecated
	void updateDeliveryStatusForNumber (
			Transaction parentTransaction,
			String numTo,
			MessageStatus status);

	@Deprecated
	NumberRec archiveNumberFromMessage (
			Transaction parentTransaction,
			MessageRec message);

}
