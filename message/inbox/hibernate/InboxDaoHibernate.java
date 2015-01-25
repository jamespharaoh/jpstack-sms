package wbs.sms.message.inbox.hibernate;

import java.util.List;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.joda.time.Instant;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.hibernate.HibernateDao;
import wbs.sms.message.inbox.model.InboxDao;
import wbs.sms.message.inbox.model.InboxRec;
import wbs.sms.message.inbox.model.InboxState;

@SingletonComponent ("inboxDao")
public
class InboxDaoHibernate
	extends HibernateDao
	implements InboxDao {

	@Override
	public
	int count () {

		return (int) (long) findOne (
			Long.class,

			createCriteria (
				InboxRec.class,
				"_inbox")

			.add (
				Restrictions.eq (
					"_inbox.state",
					InboxState.pending))

			.setProjection (
				Projections.rowCount ())

			.list ());

	}

	@Override
	public
	List<InboxRec> findPendingLimit (
			Instant now,
			int maxResults) {

		return findMany (
			InboxRec.class,

			createCriteria (
				InboxRec.class,
				"_inbox")

			.add (
				Restrictions.le (
					"_inbox.nextAttempt",
					now))

			.addOrder (
				Order.asc (
					"_inbox.nextAttempt"))

			.addOrder (
				Order.asc (
					"_inbox.id"))

			.setMaxResults (
				maxResults)

			.list ());

	}

	@Override
	public
	List<InboxRec> findPendingLimit (
			int maxResults) {

		return findMany (
			InboxRec.class,

			createCriteria (
				InboxRec.class,
				"_inbox")

			.addOrder (
				Order.desc (
					"_inbox.timestamp"))

			.addOrder (
				Order.desc (
					"_inbox.id"))

			.setMaxResults (
				maxResults)

			.list ());


	}

}
