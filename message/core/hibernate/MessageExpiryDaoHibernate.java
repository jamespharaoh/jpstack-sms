package wbs.sms.message.core.hibernate;

import java.util.Date;
import java.util.List;

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
			int maxResults) {

		return findMany (
			MessageExpiryRec.class,

			createQuery (
				"FROM MessageExpiryRec messageExpiry " +
				"WHERE messageExpiry.expiryTime < :now " +
				"ORDER BY messageExpiry.expiryTime")

			.setTimestamp (
				"now",
				new Date ())

			.setMaxResults (
				maxResults)

			.list ());

	}

}
