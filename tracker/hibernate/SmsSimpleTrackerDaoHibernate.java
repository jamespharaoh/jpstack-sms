package wbs.sms.tracker.hibernate;

import java.util.List;

import wbs.framework.hibernate.HibernateDao;
import wbs.sms.message.core.hibernate.MessageDirectionType;
import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.tracker.model.SmsSimpleTrackerDao;
import wbs.sms.tracker.model.SmsSimpleTrackerRec;

public
class SmsSimpleTrackerDaoHibernate
	extends HibernateDao
	implements SmsSimpleTrackerDao {

	@Override
	public
	List<MessageRec> findMessages (
			SmsSimpleTrackerRec smsSimpleTracker,
			NumberRec number) {

		return findMany (
			MessageRec.class,

			createQuery (
				"SELECT message " +
				"FROM MessageRec message, " +
					"SmsSimpleTrackerRouteRec smsSimpleTrackerRoute " +
				"WHERE smsSimpleTrackerRoute.smsSimpleTracker = :smsSimpleTracker " +
					"AND message.number.id = :number " +
					"AND message.direction = :direction " +
					"AND message.route = smsSimpleTrackerRoute.route " +
					"AND smsSimpleTrackerRoute.smsSimpleTracker = :smsSimpleTracker")

			.setEntity (
				"smsSimpleTracker",
				smsSimpleTracker)

			.setEntity (
				"number",
				number)

			.setParameter (
				"direction",
				MessageDirection.out,
				MessageDirectionType.INSTANCE)

			.list ());

	}

}
