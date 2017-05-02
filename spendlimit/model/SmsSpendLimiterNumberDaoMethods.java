package wbs.sms.spendlimit.model;

import java.util.List;

import wbs.framework.database.Transaction;

import wbs.sms.number.core.model.NumberRec;

public
interface SmsSpendLimiterNumberDaoMethods {

	SmsSpendLimiterNumberRec find (
			Transaction parentTransaction,
			SmsSpendLimiterRec smsSpendLimiter,
			NumberRec number);

	List <Long> searchIds (
			Transaction parentTransaction,
			SmsSpendLimiterNumberSearch search);

}
