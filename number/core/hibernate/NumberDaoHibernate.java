package wbs.sms.number.core.hibernate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import lombok.NonNull;
import wbs.framework.hibernate.HibernateDao;
import wbs.sms.number.core.model.NumberDao;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.number.core.model.NumberSearch;

public
class NumberDaoHibernate
	extends HibernateDao
	implements NumberDao {

	@Override
	public
	List <Long> searchIds (
			@NonNull NumberSearch numberSearch) {

		Criteria criteria =
			createCriteria (
				NumberRec.class);

		if (numberSearch.getNumber () != null) {

			criteria.add (
				Restrictions.like (
					"number",
					numberSearch.getNumber ()));

		}

		// add default order

		criteria

			.addOrder (
				Order.asc ("number"));

		// set to return ids only

		criteria

			.setProjection (
				Projections.id ());

		// perform and return

		return findMany (
			"searchIds (numberSearch)",
			Long.class,
			criteria);

	}

}
