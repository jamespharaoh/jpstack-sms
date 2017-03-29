package wbs.sms.number.core.model;

import java.util.List;

import wbs.framework.logging.TaskLogger;

public
interface NumberObjectHelperMethods {

	NumberRec findOrCreate (
			TaskLogger parentTaskLogger,
			String number);

	List <NumberRec> findOrCreateMany (
			TaskLogger parentTaskLogger,
			List <String> numbers);

}