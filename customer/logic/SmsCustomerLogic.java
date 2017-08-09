package wbs.sms.customer.logic;

import com.google.common.base.Optional;

import wbs.framework.database.Transaction;

import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.user.model.UserRec;

import wbs.sms.customer.model.SmsCustomerAffiliateRec;
import wbs.sms.customer.model.SmsCustomerRec;
import wbs.sms.customer.model.SmsCustomerSessionRec;
import wbs.sms.message.core.model.MessageRec;

public
interface SmsCustomerLogic {

	void sessionStart (
			Transaction parentTransaction,
			SmsCustomerRec customer,
			Optional <Long> threadId);

	void sessionEndManually (
			Transaction parentTransaction,
			UserRec user,
			SmsCustomerRec customer,
			String reason);

	void sessionTimeoutAuto (
			Transaction parentTransaction,
			SmsCustomerSessionRec session);

	Optional <AffiliateRec> customerAffiliate (
			Transaction parentTransaction,
			SmsCustomerRec customer);

	void customerAffiliateUpdate (
			Transaction parentTransaction,
			SmsCustomerRec customer,
			SmsCustomerAffiliateRec affiliate,
			MessageRec message);

}
