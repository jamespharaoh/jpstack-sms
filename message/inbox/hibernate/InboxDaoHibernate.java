package wbs.sms.message.inbox.hibernate;

import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;

import java.util.List;

import lombok.NonNull;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.LogContext;

import wbs.platform.scaffold.model.SliceRec;

import wbs.sms.message.inbox.model.InboxDao;
import wbs.sms.message.inbox.model.InboxRec;
import wbs.sms.message.inbox.model.InboxState;
import wbs.sms.route.core.model.RouteRec;

@SingletonComponent ("inboxDao")
public
class InboxDaoHibernate
	extends HibernateDao
	implements InboxDao {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	Long countPending (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"countPending");

		) {

			return findOneOrNull (
				transaction,
				Long.class,

				createCriteria (
					transaction,
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

	}

	@Override
	public
	Long countPendingOlderThan (
			@NonNull Transaction parentTransaction,
			@NonNull SliceRec slice,
			@NonNull Instant instant) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"countPendingOlderThan");

		) {

			return findOneOrNull (
				transaction,
				Long.class,

				createCriteria (
					transaction,
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

	}

	@Override
	public
	Long countPendingOlderThan (
			@NonNull Transaction parentTransaction,
			@NonNull RouteRec route,
			@NonNull Instant instant) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"countPendingOlderThan");

		) {

			return findOneOrNull (
				transaction,
				Long.class,

				createCriteria (
					transaction,
					InboxRec.class,
					"_inbox")

				.add (
					Restrictions.eq (
						"_inbox.route",
						route))

				.add (
					Restrictions.sqlRestriction (
						"{alias}.\"state\" = 'pending'::inbox_state"))

				.add (
					Restrictions.lt (
						"_inbox.createdTime",
						instant))

				.setProjection (
					Projections.rowCount ())

			);

		}

	}

	@Override
	public
	List <InboxRec> findPendingLimit (
			@NonNull Transaction parentTransaction,
			@NonNull Instant now,
			@NonNull Long maxResults) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findPendingLimit");

		) {

			return findMany (
				transaction,
				InboxRec.class,

				createCriteria (
					transaction,
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

	}

	@Override
	public
	List <InboxRec> findPendingLimit (
			@NonNull Transaction parentTransaction,
			@NonNull Long maxResults) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findPendingLimit");

		) {

			return findMany (
				transaction,
				InboxRec.class,

				createCriteria (
					transaction,
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

}
