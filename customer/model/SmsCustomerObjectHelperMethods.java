package wbs.sms.customer.model;

import wbs.framework.logging.TaskLogger;

import wbs.sms.number.core.model.NumberRec;

public
interface SmsCustomerObjectHelperMethods {

	SmsCustomerRec findOrCreate (
			TaskLogger parentTaskLogger,
			SmsCustomerManagerRec manager,
			NumberRec number);

}