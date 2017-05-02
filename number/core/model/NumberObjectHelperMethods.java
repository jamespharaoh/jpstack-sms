package wbs.sms.number.core.model;

import java.util.List;

import wbs.framework.database.Transaction;

public
interface NumberObjectHelperMethods {

	NumberRec findOrCreate (
			Transaction parentTransaction,
			String number);

	List <NumberRec> findOrCreateMany (
			Transaction parentTransaction,
			List <String> numbers);

}