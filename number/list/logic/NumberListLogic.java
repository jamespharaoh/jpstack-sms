package wbs.sms.number.list.logic;

import wbs.platform.service.model.ServiceRec;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.number.list.model.NumberListRec;

public
interface NumberListLogic {

	void addDueToMessage (
			NumberListRec numberList,
			NumberRec number,
			MessageRec message,
			ServiceRec service);

	void removeDueToMessage (
			NumberListRec numberList,
			NumberRec number,
			MessageRec message,
			ServiceRec service);

	boolean includesNumber (
			NumberListRec numberList,
			NumberRec number);

}
