package wbs.sms.number.list.model;

import java.util.List;

import wbs.sms.number.core.model.NumberRec;

public
interface NumberListNumberDaoMethods {

	NumberListNumberRec find (
			NumberListRec numberList,
			NumberRec number);

	List <NumberListNumberRec> findManyPresent (
			NumberListRec numberList,
			List <NumberRec> numbers);

}