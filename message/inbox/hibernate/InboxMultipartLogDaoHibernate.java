package wbs.sms.message.inbox.hibernate;

import java.util.List;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;
import org.joda.time.Instant;

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
			@NonNull InboxMultipartBufferRec inboxMultipartBuffer,
			@NonNull Instant timestamp) {

		return findMany (
			"findRecent (inboxMultipartBuffer, timestamp)",
			InboxMultipartLogRec.class,

			createCriteria (
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
