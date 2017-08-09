package wbs.sms.spendlimit.model;

import wbs.framework.database.Transaction;

import wbs.sms.number.core.model.NumberRec;

public
interface SmsSpendLimiterNumberObjectHelperMethods {

	SmsSpendLimiterNumberRec findOrCreate (
			Transaction parentTransaction,
			SmsSpendLimiterRec smsSpendLimiter,
			NumberRec number);

}
