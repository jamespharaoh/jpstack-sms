package wbs.sms.message.inbox.hibernate;

import java.util.Date;
import java.util.List;

import wbs.framework.hibernate.HibernateDao;
import wbs.sms.message.inbox.model.InboxMultipartBufferDao;
import wbs.sms.message.inbox.model.InboxMultipartBufferRec;
import wbs.sms.route.core.model.RouteRec;

public
class InboxMultipartBufferDaoHibernate
	extends HibernateDao
	implements InboxMultipartBufferDao {

	@Override
	public
	List<InboxMultipartBufferRec> findByOtherId (
			RouteRec route,
			String otherId) {

		return findMany (
			InboxMultipartBufferRec.class,

			createQuery (
				"FROM InboxMultipartBufferRec inboxMultipartBuffer " +
				"WHERE inboxMultipartBuffer.route = :route " +
					"AND inboxMultipartBuffer.msgOtherId = :otherId")

			.setEntity (
				"route",
				route)

			.setString (
				"otherId",
				otherId)

			.list ());

	}

	@Override
	public
	List<InboxMultipartBufferRec> findRecent (
			InboxMultipartBufferRec inboxMultipartBuffer,
			Date timestamp) {

		return findMany (
			InboxMultipartBufferRec.class,

			createQuery (
				"FROM InboxMultipartBufferRec inboxMultipartBuffer " +
				"WHERE inboxMultipartBuffer.route = :route " +
					"AND inboxMultipartBuffer.msgFrom = :msgFrom " +
					"AND inboxMultipartBuffer.multipartId = :multipartId " +
					"AND inboxMultipartBuffer.multipartSegMax = :multipartSegMax " +
					"AND inboxMultipartBuffer.timestamp >= :timestamp")

			.setEntity (
				"route",
				inboxMultipartBuffer.getRoute ())

			.setString (
				"msgFrom",
				inboxMultipartBuffer.getMsgFrom ())

			.setInteger (
				"multipartId",
				inboxMultipartBuffer.getMultipartId ())

			.setInteger (
				"multipartSegMax",
				inboxMultipartBuffer.getMultipartSegMax ())

			.setTimestamp (
				"timestamp",
				timestamp)

			.list ());

	}

}
