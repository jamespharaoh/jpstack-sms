package wbs.sms.number.core.hibernate;

import java.util.List;

import lombok.NonNull;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDaoLegacy;
import wbs.framework.logging.LogContext;

import wbs.sms.number.core.model.NumberDao;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.number.core.model.NumberSearch;

public
class NumberDaoHibernate
	extends HibernateDaoLegacy
	implements NumberDao {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	List <Long> searchIds (
			@NonNull Transaction parentTransaction,
			@NonNull NumberSearch numberSearch) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"searchIds");

		) {

			Criteria criteria =
				createCriteria (
					transaction,
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
				transaction,
				Long.class,
				criteria);

		}

	}

}
