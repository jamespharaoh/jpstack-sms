package wbs.sms.number.list.logic;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import wbs.framework.logging.TaskLogger;

import wbs.platform.service.model.ServiceRec;

import wbs.sms.message.core.model.MessageRec;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.number.list.model.NumberListRec;

public
interface NumberListLogic {

	void addDueToMessage (
			TaskLogger parentTaskLogger,
			NumberListRec numberList,
			NumberRec number,
			MessageRec message,
			ServiceRec service);

	void removeDueToMessage (
			TaskLogger parentTaskLogger,
			NumberListRec numberList,
			NumberRec number,
			MessageRec message,
			ServiceRec service);

	boolean includesNumber (
			NumberListRec numberList,
			NumberRec number);

	Pair <List <NumberRec>, List <NumberRec>> splitNumbersPresent (
			NumberListRec numberList,
			List <NumberRec> numbers);

}
