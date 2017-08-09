package wbs.sms.message.inbox.hibernate;

import java.util.List;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;
import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.LogContext;

import wbs.sms.message.inbox.model.InboxMultipartBufferDao;
import wbs.sms.message.inbox.model.InboxMultipartBufferRec;
import wbs.sms.route.core.model.RouteRec;

public
class InboxMultipartBufferDaoHibernate
	extends HibernateDao
	implements InboxMultipartBufferDao {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	List <InboxMultipartBufferRec> findByOtherId (
			@NonNull Transaction parentTransaction,
			@NonNull RouteRec route,
			@NonNull String otherId) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findByOtherId");

		) {

			return findMany (
				transaction,
				InboxMultipartBufferRec.class,

				createCriteria (
					transaction,
					InboxMultipartBufferRec.class,
					"_inboxMultipartBuffer")

				.add (
					Restrictions.eq (
						"_inboxMultipartBuffer.route",
						route))

				.add (
					Restrictions.eq (
						"_inboxMultipartBuffer.msgOtherId",
						otherId))

			);

		}

	}

	@Override
	public
	List <InboxMultipartBufferRec> findRecent (
			@NonNull Transaction parentTransaction,
			@NonNull InboxMultipartBufferRec inboxMultipartBuffer,
			@NonNull Instant timestamp) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findRecent");

		) {

			return findMany (
				transaction,
				InboxMultipartBufferRec.class,

				createCriteria (
					transaction,
					InboxMultipartBufferRec.class,
					"_inboxMultipartBuffer")

				.add (
					Restrictions.eq (
						"_inboxMultipartBuffer.route",
						inboxMultipartBuffer.getRoute ()))

				.add (
					Restrictions.eq (
						"_inboxMultipartBuffer.msgFrom",
						inboxMultipartBuffer.getMsgFrom ()))

				.add (
					Restrictions.eq (
						"_inboxMultipartBuffer.multipartId",
						inboxMultipartBuffer.getMultipartId ()))

				.add (
					Restrictions.eq (
						"_inboxMultipartBuffer.multipartSegMax",
						inboxMultipartBuffer.getMultipartSegMax ()))

				.add (
					Restrictions.ge (
						"_inboxMultipartBuffer.timestamp",
						timestamp))

			);

		}

	}

}
