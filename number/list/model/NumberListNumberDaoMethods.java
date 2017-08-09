package wbs.sms.number.list.model;

import java.util.List;

import wbs.framework.database.Transaction;

import wbs.sms.number.core.model.NumberRec;

public
interface NumberListNumberDaoMethods {

	NumberListNumberRec find (
			Transaction parentTransaction,
			NumberListRec numberList,
			NumberRec number);

	List <NumberListNumberRec> findManyPresent (
			Transaction parentTransaction,
			NumberListRec numberList,
			List <NumberRec> numbers);

	List <Long> searchIds (
			Transaction parentTransaction,
			NumberListNumberSearch search);

}