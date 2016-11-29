package wbs.sms.spendlimit.model;

import wbs.sms.number.core.model.NumberRec;

public
interface SmsSpendLimiterNumberObjectHelperMethods {

	SmsSpendLimiterNumberRec findOrCreate (
			SmsSpendLimiterRec smsSpendLimiter,
			NumberRec number);

}
