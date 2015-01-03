package wbs.sms.customer.hibernate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import wbs.framework.application.annotations.SingletonComponent;
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
	List<Integer> searchIds (
			SmsCustomerSearch search) {

		Criteria customerCriteria =
			createCriteria (
				SmsCustomerRec.class);

		Criteria managerCriteria =
			customerCriteria.createCriteria (
				"smsCustomerManager");

		Criteria numberCriteria =
			customerCriteria.createCriteria (
				"number");

		if (search.getSmsCustomerManagerId () != null) {

			managerCriteria.add (
				Restrictions.eq (
					"id",
					search.getSmsCustomerManagerId ()));

		}

		if (search.getNumberLike () != null) {

			numberCriteria.add (
				Restrictions.ilike (
					"number",
					search.getNumberLike ()));

		}

		managerCriteria.addOrder (
			Order.asc ("code"));

		customerCriteria.addOrder (
			Order.asc ("code"));

		customerCriteria.setProjection (
			Projections.id ());

		return findMany (
			Integer.class,
			customerCriteria.list ());

	}

	@Override
	public
	SmsCustomerRec find (
			SmsCustomerManagerRec manager,
			NumberRec number) {

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

		return findOne (
			SmsCustomerRec.class,
			customerCriteria.list ());

	}

}
