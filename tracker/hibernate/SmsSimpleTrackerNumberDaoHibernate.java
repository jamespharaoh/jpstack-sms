package wbs.sms.tracker.hibernate;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDaoLegacy;
import wbs.framework.logging.LogContext;

import wbs.sms.number.core.model.NumberRec;
import wbs.sms.tracker.model.SmsSimpleTrackerNumberDao;
import wbs.sms.tracker.model.SmsSimpleTrackerNumberRec;
import wbs.sms.tracker.model.SmsSimpleTrackerRec;

public
class SmsSimpleTrackerNumberDaoHibernate
	extends HibernateDaoLegacy
	implements SmsSimpleTrackerNumberDao {

	// singleton dependency

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	SmsSimpleTrackerNumberRec find (
			@NonNull Transaction parentTransaction,
			@NonNull SmsSimpleTrackerRec smsSimpleTracker,
			@NonNull NumberRec number) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"find");

		) {

			return findOneOrNull (
				transaction,
				SmsSimpleTrackerNumberRec.class,

				createCriteria (
					transaction,
					SmsSimpleTrackerNumberRec.class,
					"_smsSimpleTrackerNumber")

				.add (
					Restrictions.eq (
						"_smsSimpleTrackerNumber.smsSimpleTracker",
						smsSimpleTracker))

				.add (
					Restrictions.eq (
						"_smsSimpleTrackerNumber.number",
						number))

			);

		}

	}

}
