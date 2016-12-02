package wbs.sms.tracker.hibernate;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;

import wbs.framework.hibernate.HibernateDao;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.tracker.model.SmsSimpleTrackerNumberDao;
import wbs.sms.tracker.model.SmsSimpleTrackerNumberRec;
import wbs.sms.tracker.model.SmsSimpleTrackerRec;

public
class SmsSimpleTrackerNumberDaoHibernate
	extends HibernateDao
	implements SmsSimpleTrackerNumberDao {

	@Override
	public
	SmsSimpleTrackerNumberRec find (
			@NonNull SmsSimpleTrackerRec smsSimpleTracker,
			@NonNull NumberRec number) {

		return findOneOrNull (
			"find (smsSimpleTracker, number)",
			SmsSimpleTrackerNumberRec.class,

			createCriteria (
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
