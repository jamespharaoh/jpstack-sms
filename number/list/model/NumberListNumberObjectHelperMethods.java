package wbs.sms.number.list.model;

import wbs.sms.number.core.model.NumberRec;

public
interface NumberListNumberObjectHelperMethods {

	NumberListNumberRec find (
			NumberListRec numberList,
			NumberRec number);

	NumberListNumberRec findOrCreate (
			NumberListRec numberList,
			NumberRec number);

}