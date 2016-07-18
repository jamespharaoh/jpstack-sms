package wbs.sms.number.list.model;

import wbs.sms.number.core.model.NumberRec;

public
interface NumberListNumberObjectHelperMethods {

	NumberListNumberRec findOrCreate (
			NumberListRec numberList,
			NumberRec number);

}