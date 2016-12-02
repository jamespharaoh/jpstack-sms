package wbs.sms.modempoll.hibernate;

import lombok.NonNull;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
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
			@NonNull Instant now) {

		return findOneOrNull (
			"findNext (now)",
			ModemPollQueueRec.class,

			createCriteria (
				ModemPollQueueRec.class,
				"_modemPollQueue")

			.add (
				Restrictions.le (
					"_modemPollQueue.retryTime",
					now))

			.addOrder (
				Order.asc (
					"_modemPollQueue.retryTime"))

			.setMaxResults (
				1)

		);

	}

}
