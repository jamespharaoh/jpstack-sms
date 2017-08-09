package wbs.sms.customer.model;

import java.util.List;

import wbs.framework.database.Transaction;

import wbs.sms.number.core.model.NumberRec;

public
interface SmsCustomerDaoMethods {

	List <Long> searchIds (
			Transaction parentTransaction,
			SmsCustomerSearch search);

	SmsCustomerRec find (
			Transaction parentTransaction,
			SmsCustomerManagerRec manager,
			NumberRec number);

}