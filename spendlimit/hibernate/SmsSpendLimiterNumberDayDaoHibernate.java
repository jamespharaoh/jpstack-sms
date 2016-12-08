package wbs.sms.spendlimit.hibernate;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;
import org.joda.time.LocalDate;

import wbs.framework.hibernate.HibernateDao;

import wbs.sms.spendlimit.model.SmsSpendLimiterNumberDayDaoMethods;
import wbs.sms.spendlimit.model.SmsSpendLimiterNumberDayRec;
import wbs.sms.spendlimit.model.SmsSpendLimiterNumberRec;

public
class SmsSpendLimiterNumberDayDaoHibernate
	extends HibernateDao
	implements SmsSpendLimiterNumberDayDaoMethods {

	@Override
	public
	SmsSpendLimiterNumberDayRec find (
			@NonNull SmsSpendLimiterNumberRec smsSpendLimiterNumber,
			@NonNull LocalDate date) {

		return findOneOrNull (
			"find (smsSpendLimiterNumber, date)",
			SmsSpendLimiterNumberDayRec.class,

			createCriteria (
				SmsSpendLimiterNumberDayRec.class,
				"_smsSpendLimiterNumberDay")

			.add (
				Restrictions.eq (
					"_smsSpendLimiterNumberDay.smsSpendLimiterNumber",
					smsSpendLimiterNumber))

			.add (
				Restrictions.eq (
					"_smsSpendLimiterNumberDay.date",
					date))

		);

	}

}
