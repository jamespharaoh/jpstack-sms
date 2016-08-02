package wbs.sms.message.outbox.hibernate;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.NonNull;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.joda.time.Instant;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.hibernate.TimestampWithTimezoneUserType;
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
			"count ()",
			Long.class,

			createCriteria (
				OutboxRec.class,
				"_outbox")

			.setProjection (
				Projections.rowCount ())

		);

	}

	@Override
	public
	OutboxRec find (
			@NonNull MessageRec message) {

		return get (
			OutboxRec.class,
			message.getId ());

	}

	@Override
	public
	List<OutboxRec> findLimit (
			@NonNull RouteRec route,
			long maxResults) {

		return findMany (
			"findLimit (route, maxResults)",
			OutboxRec.class,

			createCriteria (
				OutboxRec.class)

			.add (
				Restrictions.eq (
					"route",
					route))

			.addOrder (
				Order.asc (
					"id"))

			.setMaxResults (
				(int) maxResults)

		);

	}

	@Override
	public
	OutboxRec findNext (
			@NonNull Instant now,
			@NonNull RouteRec route) {

		return findOne (
			"findNext (now, route)",
			OutboxRec.class,

			createCriteria (
				OutboxRec.class,
				"_outbox")

			.createAlias (
				"_outbox.message",
				"_message")

			.createAlias (
				"_message.number",
				"_number")

			.add (
				Restrictions.le (
					"_outbox.retryTime",
					now))

			.add (
				Restrictions.eq (
					"_outbox.route",
					route))

			.add (
				Restrictions.isNull (
					"_outbox.sending"))

			.add (
				Restrictions.isNull (
					"_number.archiveDate"))

			.add (
				Restrictions.or (

				Restrictions.isNull (
					"_outbox.remainingTries"),

				Restrictions.gt (
					"_outbox.remainingTries",
					0l)

			))

			.addOrder (
				Order.asc (
					"_outbox.pri"))

			.addOrder (
				Order.asc (
					"_outbox.retryTime"))

			.setMaxResults (
				1)

		);

	}

	@Override
	public
	List<OutboxRec> findNextLimit (
			@NonNull Instant now,
			@NonNull RouteRec route,
			long maxResults) {

		return findMany (
			"findNextLimit (now, route, maxResults)",
			OutboxRec.class,

			createCriteria (
				OutboxRec.class,
				"_outbox")

			.createAlias (
				"_outbox.message",
				"_message")

			.createAlias (
				"_message.number",
				"_number")

			.add (
				Restrictions.lt (
					"_outbox.retryTime",
					now))

			.add (
				Restrictions.eq (
					"_outbox.route",
					route))

			.add (
				Restrictions.isNull (
					"_outbox.sending"))

			.add (
				Restrictions.isNull (
					"_number.archiveDate"))

			.add (
				Restrictions.or (

				Restrictions.isNull (
					"_outbox.remainingTries"),

				Restrictions.gt (
					"_outbox.remainingTries",
					0l)

			))

			.addOrder (
				Order.asc (
					"_outbox.pri"))

			.addOrder (
				Order.asc (
					"_outbox.retryTime"))

			.setMaxResults (
				(int) maxResults)

		);

	}

	@Override
	public
	Map<Long,Long> generateRouteSummary (
			@NonNull Instant now) {

		@SuppressWarnings ("unchecked")
		List<Object[]> list =
			createQuery (
				stringFormat (
					"SELECT ",
						"outbox.route.id, ",
						"count (*) ",
					"FROM OutboxRec outbox ",
					"WHERE outbox.retryTime < :date ",
					"AND (",
						"outbox.remainingTries IS NULL ",
						"OR outbox.remainingTries > 0",
					") ",
					"AND outbox.sending IS NULL ",
					"GROUP BY outbox.route.id"))

			.setParameter (
				"date",
				now,
				TimestampWithTimezoneUserType.INSTANCE)

			.list ();

		Map<Long,Long> map =
			new HashMap<Long,Long> ();

		for (
			Object[] row
				: list
		) {

			map.put (
				(long) (Integer) row [0],
				(long) (Long) row [1]);

		}

		return map;

	}

	@Override
	public
	List<OutboxRec> findSendingBeforeLimit (
			@NonNull Instant sendingBefore,
			long maxResults) {

		return findMany (
			"findSendingBeforeLimit (sendingBefore, maxResults)",
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
					sendingBefore))

			.addOrder (
				Order.asc (
					"_outbox.sending"))

			.setMaxResults (
				(int) maxResults)

		);

	}

}
