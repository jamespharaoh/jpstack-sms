package wbs.sms.customer.logic;

import com.google.common.base.Optional;

import wbs.sms.customer.model.SmsCustomerRec;
import wbs.sms.customer.model.SmsCustomerSessionRec;

public
interface SmsCustomerLogic {

	void sessionStart (
			SmsCustomerRec customer,
			Optional<Long> threadId);

	void sessionTimeoutAuto (
			SmsCustomerSessionRec session);

}
