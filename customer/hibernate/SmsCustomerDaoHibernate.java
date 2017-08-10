package wbs.sms.customer.hibernate;

import java.util.List;

import lombok.NonNull;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDaoLegacy;
import wbs.framework.logging.LogContext;

import wbs.sms.customer.model.SmsCustomerDao;
import wbs.sms.customer.model.SmsCustomerManagerRec;
import wbs.sms.customer.model.SmsCustomerRec;
import wbs.sms.customer.model.SmsCustomerSearch;
import wbs.sms.number.core.model.NumberRec;

@SingletonComponent ("smsCustomerDaoHibernate")
public
class SmsCustomerDaoHibernate
	extends HibernateDaoLegacy
	implements SmsCustomerDao {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	List <Long> searchIds (
			@NonNull Transaction parentTransaction,
			@NonNull SmsCustomerSearch smsCustomerSearch) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"searchIds");

		) {

			Criteria customerCriteria =
				createCriteria (
					transaction,
					SmsCustomerRec.class);

			Criteria managerCriteria =
				customerCriteria.createCriteria (
					"smsCustomerManager");

			Criteria numberCriteria =
				customerCriteria.createCriteria (
					"number");

			if (smsCustomerSearch.getSmsCustomerManagerId () != null) {

				managerCriteria.add (
					Restrictions.eq (
						"id",
						smsCustomerSearch.getSmsCustomerManagerId ()));

			}

			if (smsCustomerSearch.getNumberLike () != null) {

				numberCriteria.add (
					Restrictions.ilike (
						"number",
						smsCustomerSearch.getNumberLike ()));

			}

			managerCriteria.addOrder (
				Order.asc ("code"));

			customerCriteria.addOrder (
				Order.asc ("code"));

			customerCriteria.setProjection (
				Projections.id ());

			return findMany (
				transaction,
				Long.class,
				customerCriteria);

		}

	}

	@Override
	public
	SmsCustomerRec find (
			@NonNull Transaction parentTransaction,
			@NonNull SmsCustomerManagerRec manager,
			@NonNull NumberRec number) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"find");

		) {

			Criteria customerCriteria =
				createCriteria (
					transaction,
					SmsCustomerRec.class);

			customerCriteria.add (
				Restrictions.eq (
					"smsCustomerManager",
					manager));

			customerCriteria.add (
				Restrictions.eq (
					"number",
					number));

			return findOneOrNull (
				transaction,
				SmsCustomerRec.class,
				customerCriteria);

		}

	}

}
