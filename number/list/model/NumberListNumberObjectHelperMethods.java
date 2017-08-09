package wbs.sms.number.list.model;

import wbs.framework.database.Transaction;

import wbs.sms.number.core.model.NumberRec;

public
interface NumberListNumberObjectHelperMethods {

	NumberListNumberRec findOrCreate (
			Transaction parentTransaction,
			NumberListRec numberList,
			NumberRec number);

}