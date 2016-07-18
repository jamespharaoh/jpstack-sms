package wbs.sms.message.inbox.hibernate;

import java.util.Date;
import java.util.List;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;

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
			@NonNull RouteRec route,
			@NonNull String otherId) {

		return findMany (
			"findByOtherId (route, otherId)",
			InboxMultipartBufferRec.class,

			createCriteria (
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

	@Override
	public
	List<InboxMultipartBufferRec> findRecent (
			@NonNull InboxMultipartBufferRec inboxMultipartBuffer,
			@NonNull Date timestamp) {

		return findMany (
			"findRecent (inboxMultipartBuffer, timestamp)",
			InboxMultipartBufferRec.class,

			createCriteria (
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
