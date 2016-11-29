package wbs.sms.spendlimit.model;

import org.joda.time.LocalDate;

public
interface SmsSpendLimiterNumberDayDaoMethods {

	SmsSpendLimiterNumberDayRec find (
			SmsSpendLimiterNumberRec smsSpendLimiterNumber,
			LocalDate date);

}
