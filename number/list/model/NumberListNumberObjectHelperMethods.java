package wbs.sms.number.list.model;

import wbs.framework.logging.TaskLogger;

import wbs.sms.number.core.model.NumberRec;

public
interface NumberListNumberObjectHelperMethods {

	NumberListNumberRec findOrCreate (
			TaskLogger parentTaskLogger,
			NumberListRec numberList,
			NumberRec number);

}