package wbs.sms.customer.hibernate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import lombok.NonNull;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.hibernate.HibernateDao;
import wbs.sms.customer.model.SmsCustomerDao;
import wbs.sms.customer.model.SmsCustomerManagerRec;
import wbs.sms.customer.model.SmsCustomerRec;
import wbs.sms.customer.model.SmsCustomerSearch;
import wbs.sms.number.core.model.NumberRec;

@SingletonComponent ("smsCustomerDaoHibernate")
public
class SmsCustomerDaoHibernate
	extends HibernateDao
	implements SmsCustomerDao {

	@Override
	public
	List <Long> searchIds (
			@NonNull SmsCustomerSearch smsCustomerSearch) {

		Criteria customerCriteria =
			createCriteria (
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
			"searchIds (smsCustomerSearch)",
			Long.class,
			customerCriteria);

	}

	@Override
	public
	SmsCustomerRec find (
			@NonNull SmsCustomerManagerRec manager,
			@NonNull NumberRec number) {

		Criteria customerCriteria =
			createCriteria (
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
			"find (manager, number)",
			SmsCustomerRec.class,
			customerCriteria);

	}

}
