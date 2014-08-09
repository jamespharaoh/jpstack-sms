package wbs.sms.message.inbox.hibernate;

import java.util.Date;
import java.util.List;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.hibernate.HibernateDao;
import wbs.sms.message.inbox.model.InboxDao;
import wbs.sms.message.inbox.model.InboxRec;

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

			createQuery (
				"SELECT count (*) " +
				"FROM InboxRec")

			.list ());

	}

	@Override
	public
	List<InboxRec> findRetryLimit (
			int maxResults) {

		return findMany (
			InboxRec.class,

			createQuery (
				"FROM InboxRec inbox " +
				"WHERE inbox.retryTime <= :retryTime " +
				"ORDER BY inbox.id")

			.setTimestamp (
				"retryTime",
				new Date ())

			.setMaxResults (
				maxResults)

			.list ());

	}

	@Override
	public
	List<InboxRec> findAllLimit (
			int maxResults) {

		return findMany (
			InboxRec.class,

			createQuery (
				"FROM InboxRec inbox " +
				"ORDER BY inbox.id")

			.setMaxResults (
				maxResults)

			.list ());

	}

}
