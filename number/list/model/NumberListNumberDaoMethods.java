package wbs.sms.number.list.model;

import wbs.sms.number.core.model.NumberRec;

public
interface NumberListNumberDaoMethods {

	NumberListNumberRec find (
			NumberListRec numberList,
			NumberRec number);

}