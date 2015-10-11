package wbs.sms.customer.model;

import wbs.sms.number.core.model.NumberRec;

public
interface SmsCustomerObjectHelperMethods {

	SmsCustomerRec findOrCreate (
			SmsCustomerManagerRec manager,
			NumberRec number);

}