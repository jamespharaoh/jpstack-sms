package wbs.sms.message.inbox.hibernate;

import java.util.Date;
import java.util.List;

import wbs.framework.hibernate.HibernateDao;
import wbs.sms.message.inbox.model.InboxMultipartBufferRec;
import wbs.sms.message.inbox.model.InboxMultipartLogDao;
import wbs.sms.message.inbox.model.InboxMultipartLogRec;

public
class InboxMultipartLogDaoHibernate
	extends HibernateDao
	implements InboxMultipartLogDao {

	@Override
	public
	List<InboxMultipartLogRec> findRecent (
			InboxMultipartBufferRec inboxMultipartBuffer,
			Date timestamp) {

		return findMany (
			InboxMultipartLogRec.class,

			createQuery (
				"FROM InboxMultipartLogRec inboxMultipartLog " +
				"WHERE inboxMultipartLog.route = :route " +
					"AND inboxMultipartLog.msgFrom = :msgFrom " +
					"AND inboxMultipartLog.multipartId = :multipartId " +
					"AND inboxMultipartLog.multipartSegMax = :multipartSegMax " +
					"AND inboxMultipartLog.timestamp > :timestamp")

			.setEntity (
				"route",
				inboxMultipartBuffer.getRoute ())

			.setString (
				"msgFrom",
				inboxMultipartBuffer.getMsgFrom ())

			.setInteger (
				"multipartId",
				(int) (long)
				inboxMultipartBuffer.getMultipartId ())

			.setInteger (
				"multipartSegMax",
				(int) (long)
				inboxMultipartBuffer.getMultipartSegMax ())

			.setTimestamp (
				"timestamp",
				timestamp)

			.list ());

	}

}
