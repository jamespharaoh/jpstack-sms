package wbs.sms.customer.hibernate;

import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;

import java.util.List;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;
import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDaoLegacy;
import wbs.framework.logging.LogContext;

import wbs.sms.customer.model.SmsCustomerManagerRec;
import wbs.sms.customer.model.SmsCustomerSessionDao;
import wbs.sms.customer.model.SmsCustomerSessionRec;

public
class SmsCustomerSessionDaoHibernate
	extends HibernateDaoLegacy
	implements SmsCustomerSessionDao {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	List <SmsCustomerSessionRec> findToTimeoutLimit (
			@NonNull Transaction parentTransaction,
			@NonNull SmsCustomerManagerRec manager,
			@NonNull Instant startedBefore,
			@NonNull Long maxResults) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findToTimeoutLimit");

		) {

			return findMany (
				transaction,
				SmsCustomerSessionRec.class,

				createCriteria (
					transaction,
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
					toJavaIntegerRequired (
						maxResults))

			);

		}

	}

}
