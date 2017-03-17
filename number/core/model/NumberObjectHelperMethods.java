package wbs.sms.number.core.model;

import java.util.List;

public
interface NumberObjectHelperMethods {

	NumberRec findOrCreate (
			String number);

	List <NumberRec> findOrCreateMany (
			List <String> numbers);

}