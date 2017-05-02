package wbs.sms.spendlimit.model;

import org.joda.time.LocalDate;

import wbs.framework.database.Transaction;

public
interface SmsSpendLimiterNumberDayDaoMethods {

	SmsSpendLimiterNumberDayRec find (
			Transaction parentTransaction,
			SmsSpendLimiterNumberRec smsSpendLimiterNumber,
			LocalDate date);

}
