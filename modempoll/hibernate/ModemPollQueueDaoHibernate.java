package wbs.sms.modempoll.hibernate;

import static wbs.framework.utils.etc.Misc.instantToDate;

import org.joda.time.Instant;

import wbs.framework.hibernate.HibernateDao;
import wbs.sms.modempoll.model.ModemPollQueueDao;
import wbs.sms.modempoll.model.ModemPollQueueRec;

public
class ModemPollQueueDaoHibernate
	extends HibernateDao
	implements ModemPollQueueDao {

	@Override
	public
	ModemPollQueueRec findNext (
			Instant now) {

		return findOne (
			ModemPollQueueRec.class,

			createQuery (
				"FROM ModemPollQueue mpq " +
				"WHERE mpq.retryTime < :now " +
				"ORDER BY mpq.retryTime")

			.setTimestamp (
				"now",
				instantToDate (
					now))

			.list ());

	}

}
