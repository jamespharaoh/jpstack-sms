package wbs.sms.message.inbox.hibernate;

import java.util.List;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;
import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDaoLegacy;
import wbs.framework.logging.LogContext;

import wbs.sms.message.inbox.model.InboxMultipartBufferRec;
import wbs.sms.message.inbox.model.InboxMultipartLogDao;
import wbs.sms.message.inbox.model.InboxMultipartLogRec;

public
class InboxMultipartLogDaoHibernate
	extends HibernateDaoLegacy
	implements InboxMultipartLogDao {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	List <InboxMultipartLogRec> findRecent (
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
				InboxMultipartLogRec.class,

				createCriteria (
					transaction,
					InboxMultipartLogRec.class,
					"_inboxMultipartLog")

				.add (
					Restrictions.eq (
						"_inboxMultipartLog.route",
						inboxMultipartBuffer.getRoute ()))

				.add (
					Restrictions.eq (
						"_inboxMultipartLog.msgFrom",
						inboxMultipartBuffer.getMsgFrom ()))

				.add (
					Restrictions.eq (
						"_inboxMultipartLog.multipartId",
						inboxMultipartBuffer.getMultipartId ()))

				.add (
					Restrictions.eq (
						"_inboxMultipartLog.multipartSegMax",
						inboxMultipartBuffer.getMultipartSegMax ()))

				.add (
					Restrictions.ge (
						"_inboxMultipartLog.timestamp",
						timestamp))

			);

		}

	}

}
