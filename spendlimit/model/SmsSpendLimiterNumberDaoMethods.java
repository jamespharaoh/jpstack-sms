package wbs.sms.spendlimit.model;

import java.util.List;

import wbs.sms.number.core.model.NumberRec;

public
interface SmsSpendLimiterNumberDaoMethods {

	SmsSpendLimiterNumberRec find (
			SmsSpendLimiterRec smsSpendLimiter,
			NumberRec number);

	List <Long> searchIds (
			SmsSpendLimiterNumberSearch search);

}
