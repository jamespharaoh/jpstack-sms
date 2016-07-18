package wbs.sms.customer.hibernate;

import java.util.List;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;
import org.joda.time.Instant;

import wbs.framework.hibernate.HibernateDao;
import wbs.sms.customer.model.SmsCustomerManagerRec;
import wbs.sms.customer.model.SmsCustomerSessionDao;
import wbs.sms.customer.model.SmsCustomerSessionRec;

public
class SmsCustomerSessionDaoHibernate
	extends HibernateDao
	implements SmsCustomerSessionDao {

	@Override
	public
	List<SmsCustomerSessionRec> findToTimeoutLimit (
			@NonNull SmsCustomerManagerRec manager,
			@NonNull Instant startedBefore,
			int maxResults) {

		return findMany (
			"findToTimeoutLimit (manager, startedBefore, maxResults)",
			SmsCustomerSessionRec.class,

			createCriteria (
				SmsCustomerSessionRec.class,
				"_session")

			.createAlias (
				"_session.customer",
				"_customer")

			.add (
				Restrictions.eq (
					"_customer.smsCustomerManager",
					manager))

			.add (
				Restrictions.isNull (
					"_session.endTime"))

			.add (
				Restrictions.lt (
					"_session.startTime",
					startedBefore))

			.setMaxResults (
				maxResults)

		);

	}

}
