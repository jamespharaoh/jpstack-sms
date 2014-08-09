package wbs.sms.modempoll.hibernate;

import java.util.Date;

import wbs.framework.hibernate.HibernateDao;
import wbs.sms.modempoll.model.ModemPollQueueDao;
import wbs.sms.modempoll.model.ModemPollQueueRec;

public
class ModemPollQueueDaoHibernate
	extends HibernateDao
	implements ModemPollQueueDao {

	@Override
	public
	ModemPollQueueRec findNext () {

		return findOne (
			ModemPollQueueRec.class,

			createQuery (
				"FROM ModemPollQueue mpq " +
				"WHERE mpq.retryTime < :now " +
				"ORDER BY mpq.retryTime")

			.setTimestamp (
				"now",
				new Date ())

			.list ());

	}

}
