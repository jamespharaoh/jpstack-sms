package wbs.sms.message.core.hibernate;

import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;

import java.util.List;

import lombok.NonNull;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDaoLegacy;
import wbs.framework.logging.LogContext;

import wbs.sms.message.core.model.MessageExpiryDao;
import wbs.sms.message.core.model.MessageExpiryRec;

public
class MessageExpiryDaoHibernate
	extends HibernateDaoLegacy
	implements MessageExpiryDao {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	List <MessageExpiryRec> findPendingLimit (
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
				MessageExpiryRec.class,

				createCriteria (
					transaction,
					MessageExpiryRec.class,
					"_messageExpiry")

				.add (
					Restrictions.le (
						"_messageExpiry.expiryTime",
						now))

				.addOrder (
					Order.asc (
						"_messageExpiry.expiryTime"))

				.setMaxResults (
					toJavaIntegerRequired (
						maxResults))

			);

		}

	}

}
