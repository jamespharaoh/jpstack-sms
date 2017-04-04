package wbs.sms.message.inbox.hibernate;

import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;

import java.util.List;

import lombok.NonNull;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.joda.time.Instant;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.hibernate.HibernateDao;

import wbs.platform.scaffold.model.SliceRec;

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
	Long countPending () {

		return findOneOrNull (
			"countPending ()",
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

		);

	}

	@Override
	public
	Long countPendingOlderThan (
			@NonNull SliceRec slice,
			@NonNull Instant instant) {

		return findOneOrNull (
			"countPending ()",
			Long.class,

			createCriteria (
				InboxRec.class,
				"_inbox")

			.createAlias (
				"_inbox.route",
				"_route")

			.add (
				Restrictions.eq (
					"_route.slice",
					slice))

			.add (
				Restrictions.eq (
					"_inbox.state",
					InboxState.pending))

			.add (
				Restrictions.lt (
					"_inbox.createdTime",
					instant))

			.setProjection (
				Projections.rowCount ())

		);

	}

	@Override
	public
	List <InboxRec> findPendingLimit (
			@NonNull Instant now,
			@NonNull Long maxResults) {

		return findMany (
			"findPendingLimit (now, maxResults)",
			InboxRec.class,

			createCriteria (
				InboxRec.class,
				"_inbox")

			.add (
				Restrictions.eq (
					"_inbox.state",
					InboxState.pending))

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
				toJavaIntegerRequired (
					maxResults))

		);

	}

	@Override
	public
	List <InboxRec> findPendingLimit (
			@NonNull Long maxResults) {

		return findMany (
			"findPendingLimit (maxResults)",
			InboxRec.class,

			createCriteria (
				InboxRec.class,
				"_inbox")

			.add (
				Restrictions.eq (
					"_inbox.state",
					InboxState.pending))

			.addOrder (
				Order.desc (
					"_inbox.createdTime"))

			.addOrder (
				Order.desc (
					"_inbox.id"))

			.setMaxResults (
				toJavaIntegerRequired (
					maxResults))

		);

	}

}
