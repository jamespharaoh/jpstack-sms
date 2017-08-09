package wbs.sms.modempoll.hibernate;

import lombok.NonNull;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDao;
import wbs.framework.logging.LogContext;

import wbs.sms.modempoll.model.ModemPollQueueDao;
import wbs.sms.modempoll.model.ModemPollQueueRec;

public
class ModemPollQueueDaoHibernate
	extends HibernateDao
	implements ModemPollQueueDao {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	ModemPollQueueRec findNext (
			@NonNull Transaction parentTransaction,
			@NonNull Instant now) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findNext");

		) {

			return findOneOrNull (
				transaction,
				ModemPollQueueRec.class,

				createCriteria (
					transaction,
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

}
