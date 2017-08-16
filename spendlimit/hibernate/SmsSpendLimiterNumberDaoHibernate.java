package wbs.sms.spendlimit.hibernate;

import static wbs.utils.etc.NullUtils.isNotNull;

import java.util.List;

import lombok.NonNull;

import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDaoLegacy;
import wbs.framework.logging.LogContext;

import wbs.sms.number.core.model.NumberRec;
import wbs.sms.spendlimit.model.SmsSpendLimiterNumberDaoMethods;
import wbs.sms.spendlimit.model.SmsSpendLimiterNumberRec;
import wbs.sms.spendlimit.model.SmsSpendLimiterNumberSearch;
import wbs.sms.spendlimit.model.SmsSpendLimiterRec;

public
class SmsSpendLimiterNumberDaoHibernate
	extends HibernateDaoLegacy
	implements SmsSpendLimiterNumberDaoMethods {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	SmsSpendLimiterNumberRec find (
			@NonNull Transaction parentTransaction,
			@NonNull SmsSpendLimiterRec smsSpendLimiter,
			@NonNull NumberRec number) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"find");

		) {

			return findOneOrNull (
				transaction,
				SmsSpendLimiterNumberRec.class,

				createCriteria (
					transaction,
					SmsSpendLimiterNumberRec.class,
					"_smsSpendLimiterNumber")

				.add (
					Restrictions.eq (
						"_smsSpendLimiterNumber.smsSpendLimiter",
						smsSpendLimiter))

				.add (
					Restrictions.eq (
						"_smsSpendLimiterNumber.number",
						number))

			);

		}

	}

	@Override
	public
	List <Long> searchIds (
			@NonNull Transaction parentTransaction,
			@NonNull SmsSpendLimiterNumberSearch search) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"searchIds");

		) {

			Criteria criteria =

				createCriteria (
					transaction,
					SmsSpendLimiterNumberRec.class,
					"_smsSpendLimiterNumber")

				.createAlias (
					"_smsSpendLimiterNumber.smsSpendLimiter",
					"_smsSpendLimiter");

			if (
				isNotNull (
					search.smsSpendLimiterId ())
			) {

				criteria.add (
					Restrictions.eq (
						"_smsSpendLimiter.id",
						search.smsSpendLimiterId ()));

			}

			if (
				isNotNull (
					search.numberLike ())
			) {

				criteria.add (
					Restrictions.ilike (
						"_smsSpendLimiterNumber.number",
						search.numberLike ()));

			}

			criteria.setProjection (
				Projections.id ());

			return findMany (
				transaction,
				Long.class,
				criteria);

		}

	}

}
