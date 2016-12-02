package wbs.sms.spendlimit.hibernate;

import static wbs.utils.etc.Misc.isNotNull;

import java.util.List;

import lombok.NonNull;

import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import wbs.framework.hibernate.HibernateDao;

import wbs.sms.number.core.model.NumberRec;
import wbs.sms.spendlimit.model.SmsSpendLimiterNumberDaoMethods;
import wbs.sms.spendlimit.model.SmsSpendLimiterNumberRec;
import wbs.sms.spendlimit.model.SmsSpendLimiterNumberSearch;
import wbs.sms.spendlimit.model.SmsSpendLimiterRec;

public
class SmsSpendLimiterNumberDaoHibernate
	extends HibernateDao
	implements SmsSpendLimiterNumberDaoMethods {

	@Override
	public 
	SmsSpendLimiterNumberRec find (
			@NonNull SmsSpendLimiterRec smsSpendLimiter,
			@NonNull NumberRec number) {

		return findOneOrNull (
			"find (smsSpendLimiter, number)",
			SmsSpendLimiterNumberRec.class,

			createCriteria (
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

	@Override
	public 
	List <Long> searchIds (
			@NonNull SmsSpendLimiterNumberSearch search) {

		Criteria criteria =

			createCriteria (
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
			"searchIds (smsSpendLimiterNumberSearch)",
			Long.class,
			criteria);
	
	}

}
