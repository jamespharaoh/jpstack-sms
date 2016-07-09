package wbs.sms.customer.logic;

import com.google.common.base.Optional;

import wbs.platform.affiliate.model.AffiliateRec;
import wbs.sms.customer.model.SmsCustomerAffiliateRec;
import wbs.sms.customer.model.SmsCustomerRec;
import wbs.sms.customer.model.SmsCustomerSessionRec;
import wbs.sms.message.core.model.MessageRec;

public
interface SmsCustomerLogic {

	void sessionStart (
			SmsCustomerRec customer,
			Optional<Long> threadId);

	void sessionTimeoutAuto (
			SmsCustomerSessionRec session);

	Optional<AffiliateRec> customerAffiliate (
			SmsCustomerRec customer);

	void customerAffiliateUpdate (
			SmsCustomerRec customer,
			SmsCustomerAffiliateRec affiliate,
			MessageRec message);

}
