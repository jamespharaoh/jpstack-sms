package wbs.sms.customer.logic;

import com.google.common.base.Optional;

import wbs.framework.logging.TaskLogger;

import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.user.model.UserRec;

import wbs.sms.customer.model.SmsCustomerAffiliateRec;
import wbs.sms.customer.model.SmsCustomerRec;
import wbs.sms.customer.model.SmsCustomerSessionRec;
import wbs.sms.message.core.model.MessageRec;

public
interface SmsCustomerLogic {

	void sessionStart (
			TaskLogger parentTaskLogger,
			SmsCustomerRec customer,
			Optional <Long> threadId);

	void sessionEndManually (
			TaskLogger parentTaskLogger,
			UserRec user,
			SmsCustomerRec customer,
			String reason);

	void sessionTimeoutAuto (
			TaskLogger parentTaskLogger,
			SmsCustomerSessionRec session);

	Optional <AffiliateRec> customerAffiliate (
			SmsCustomerRec customer);

	void customerAffiliateUpdate (
			TaskLogger parentTaskLogger,
			SmsCustomerRec customer,
			SmsCustomerAffiliateRec affiliate,
			MessageRec message);

}
