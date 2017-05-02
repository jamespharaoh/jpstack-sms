package wbs.sms.spendlimit.hibernate;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;
import org.joda.time.LocalDate;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.LogContext;

import wbs.sms.spendlimit.model.SmsSpendLimiterNumberDayDaoMethods;
import wbs.sms.spendlimit.model.SmsSpendLimiterNumberDayRec;
import wbs.sms.spendlimit.model.SmsSpendLimiterNumberRec;

public
class SmsSpendLimiterNumberDayDaoHibernate
	extends HibernateDao
	implements SmsSpendLimiterNumberDayDaoMethods {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	SmsSpendLimiterNumberDayRec find (
			@NonNull Transaction parentTransaction,
			@NonNull SmsSpendLimiterNumberRec smsSpendLimiterNumber,
			@NonNull LocalDate date) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"find");

		) {

			return findOneOrNull (
				transaction,
				SmsSpendLimiterNumberDayRec.class,

				createCriteria (
					transaction,
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

}
