package wbs.sms.number.core.logic;

import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.number.core.model.NumberRec;

public
interface NumberLogic {

	/**
	 * Magically converts the given object to a NumberRec, depending on its
	 * type. A NumberRec is returned as is, a String is replaced by looking up
	 * an existing NumberRec, or creating one as necessary.
	 */
	NumberRec objectToNumber (
			Object object);

	// TODO wtf?
	void updateDeliveryStatusForNumber (
			String numTo,
			MessageStatus status);

	NumberRec archiveNumberFromMessage (
			MessageRec message);

}
