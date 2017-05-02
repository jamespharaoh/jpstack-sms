package wbs.sms.number.list.logic;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import wbs.framework.database.Transaction;

import wbs.platform.service.model.ServiceRec;

import wbs.sms.message.core.model.MessageRec;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.number.list.model.NumberListRec;

public
interface NumberListLogic {

	void addDueToMessage (
			Transaction parentTransaction,
			NumberListRec numberList,
			NumberRec number,
			MessageRec message,
			ServiceRec service);

	void removeDueToMessage (
			Transaction parentTransaction,
			NumberListRec numberList,
			NumberRec number,
			MessageRec message,
			ServiceRec service);

	boolean includesNumber (
			Transaction parentTransaction,
			NumberListRec numberList,
			NumberRec number);

	Pair <List <NumberRec>, List <NumberRec>> splitNumbersPresent (
			Transaction parentTransaction,
			NumberListRec numberList,
			List <NumberRec> numbers);

}
