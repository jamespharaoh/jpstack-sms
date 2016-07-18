package wbs.sms.message.core.hibernate;

import java.util.List;

import lombok.NonNull;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.joda.time.Instant;

import wbs.framework.hibernate.HibernateDao;
import wbs.sms.message.core.model.MessageExpiryDao;
import wbs.sms.message.core.model.MessageExpiryRec;

public
class MessageExpiryDaoHibernate
	extends HibernateDao
	implements MessageExpiryDao {

	@Override
	public
	List<MessageExpiryRec> findPendingLimit (
			@NonNull Instant now,
			int maxResults) {

		return findMany (
			"findPendingLimit (now, maxResults)",
			MessageExpiryRec.class,

			createCriteria (
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
				maxResults)

		);

	}

}
