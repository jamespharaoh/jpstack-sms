package wbs.sms.tracker.hibernate;

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
			SmsSimpleTrackerRec smsSimpleTracker,
			NumberRec number) {

		return findOne (
			SmsSimpleTrackerNumberRec.class,

			createQuery (
				"FROM SmsSimpleTrackerNumberRec smsSimpleTrackerNumber " +
				"WHERE smsSimpleTrackerNumber.smsSimpleTracker = :smsSimpleTracker " +
					"AND smsSimpleTrackerNumber.number = :number")

			.setEntity (
				"smsSimpleTracker",
				smsSimpleTracker)

			.setEntity (
				"number",
				number)

			.list ());

	}

}
