package wbs.sms.customer.logic;

import wbs.sms.customer.model.SmsCustomerRec;

import com.google.common.base.Optional;

public
interface SmsCustomerLogic {

	void newCustomer (
			SmsCustomerRec customer,
			Optional<Integer> threadId);

}
