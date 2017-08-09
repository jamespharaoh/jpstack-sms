package wbs.sms.customer.model;

import wbs.framework.database.Transaction;

import wbs.sms.number.core.model.NumberRec;

public
interface SmsCustomerObjectHelperMethods {

	SmsCustomerRec findOrCreate (
			Transaction parentTransaction,
			SmsCustomerManagerRec manager,
			NumberRec number);

}