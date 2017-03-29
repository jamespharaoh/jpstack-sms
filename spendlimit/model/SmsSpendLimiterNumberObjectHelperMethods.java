package wbs.sms.spendlimit.model;

import wbs.framework.logging.TaskLogger;

import wbs.sms.number.core.model.NumberRec;

public
interface SmsSpendLimiterNumberObjectHelperMethods {

	SmsSpendLimiterNumberRec findOrCreate (
			TaskLogger parentTaskLogger,
			SmsSpendLimiterRec smsSpendLimiter,
			NumberRec number);

}
