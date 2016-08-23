package wbs.sms.customer.model;

import java.util.List;

import wbs.sms.number.core.model.NumberRec;

public
interface SmsCustomerDaoMethods {

	List <Long> searchIds (
			SmsCustomerSearch search);

	SmsCustomerRec find (
			SmsCustomerManagerRec manager,
			NumberRec number);

}