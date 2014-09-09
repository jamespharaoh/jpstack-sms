package wbs.sms.customer.logic;

import wbs.sms.customer.model.SmsCustomerRec;
import wbs.sms.customer.model.SmsCustomerSessionRec;

import com.google.common.base.Optional;

public
interface SmsCustomerLogic {

	void sessionStart (
			SmsCustomerRec customer,
			Optional<Integer> threadId);

	void sessionTimeoutAuto (
			SmsCustomerSessionRec session);

}
