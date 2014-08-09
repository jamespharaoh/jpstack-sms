package wbs.sms.message.outbox.hibernate;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.joda.time.Instant;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.hibernate.HibernateDao;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.model.OutboxDao;
import wbs.sms.message.outbox.model.OutboxRec;
import wbs.sms.route.core.model.RouteRec;

@SingletonComponent ("outboxDao")
public
class OutboxDaoHibernate
	extends HibernateDao
	implements OutboxDao {

	@Override
	public
	int count () {

		return (int) (long) findOne (
			Long.class,

			createQuery (
				"SELECT count (*) " +
				"FROM OutboxRec")

			.list ());

	}

	@Override
	public
	OutboxRec find (
			MessageRec message) {

		return get (
			OutboxRec.class,
			message.getId ());

	}

	@Override
	public
	List<OutboxRec> findLimit (
			RouteRec route,
			int maxResults) {

		return findMany (
			OutboxRec.class,

			createQuery (
				"FROM OutboxRec outbox " +
				"WHERE outbox.route = :route " +
				"ORDER BY outbox.id")

			.setEntity (
				"route",
				route)

			.setMaxResults (
				maxResults)

			.list ());

	}

	@Override
	public
	OutboxRec findNext (
			RouteRec route) {

		return findOne (
			OutboxRec.class,

			createQuery (
				"FROM OutboxRec o " +
				"WHERE o.retryTime < :now " +
					"AND o.route = :route " +
					"AND o.sending IS NULL " +
					"AND o.message.number.archiveDate IS NULL " +
					"AND (o.remainingTries IS NULL " +
						"OR o.remainingTries > 0) " +
				"ORDER BY o.pri, o.retryTime")

			.setTimestamp (
				"now",
				new Date ())

			.setEntity (
				"route",
				route)

			.setMaxResults (1)

			.list ());

	}

	@Override
	public
	List<OutboxRec> findNextLimit (
			RouteRec route,
			int maxResults) {

		return findMany (
			OutboxRec.class,

			createQuery (
				"FROM OutboxRec o " +
				"WHERE o.retryTime < :now " +
					"AND o.route = :route " +
					"AND o.sending IS NULL " +
					"AND o.message.number.archiveDate IS NULL " +
					"AND (o.remainingTries IS NULL " +
						"OR o.remainingTries > 0) " +
				"ORDER BY o.pri, o.retryTime")

			.setTimestamp (
				"now",
				new Date ())

			.setEntity (
				"route",
				route)

			.setMaxResults (
				maxResults)

			.list ());

	}

	@Override
	public
	Map<Integer,Integer> generateRouteSummary () {

		@SuppressWarnings ("unchecked")
		List<Object[]> list =

			createQuery (
				"SELECT o.route.id, count (*) " +
				"FROM OutboxRec o " +
				"WHERE o.retryTime < :date " +
				"AND o.sending IS NULL " +
				"GROUP BY o.route.id")

			.setTimestamp (
				"date",
				new Date ())

			.list ();

		Map<Integer,Integer> map =
			new HashMap<Integer,Integer> ();

		for (Object[] row : list) {

			map.put (
				(Integer) row [0],
				(int) (long) (Long) row [1]);

		}

		return map;

	}

	@Override
	public
	List<OutboxRec> findSendingBeforeLimit (
			Instant sendingBefore,
			int maxResults) {

		return findMany (
			OutboxRec.class,

			createCriteria (
				OutboxRec.class,
				"_outbox")

			.add (
				Restrictions.isNotNull (
					"_outbox.sending"))

			.add (
				Restrictions.lt (
					"_outbox.sending",
					sendingBefore.toDate ()))

			.addOrder (
				Order.asc (
					"_outbox.sending"))

			.setMaxResults (
				maxResults)

			.list ()

		);

	}

}
