package wbs.sms.message.core.hibernate;

import static wbs.framework.utils.etc.Misc.instantToDate;

import java.util.List;

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
			Instant now,
			int maxResults) {

		return findMany (
			MessageExpiryRec.class,

			createQuery (
				"FROM MessageExpiryRec messageExpiry " +
				"WHERE messageExpiry.expiryTime < :now " +
				"ORDER BY messageExpiry.expiryTime")

			.setTimestamp (
				"now",
				instantToDate (
					now))

			.setMaxResults (
				maxResults)

			.list ());

	}

}
