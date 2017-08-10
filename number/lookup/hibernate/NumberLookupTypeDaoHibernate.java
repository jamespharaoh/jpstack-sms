package wbs.sms.number.lookup.hibernate;

import java.util.List;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDaoLegacy;
import wbs.framework.logging.LogContext;

import wbs.sms.number.lookup.model.NumberLookupTypeDao;
import wbs.sms.number.lookup.model.NumberLookupTypeRec;

public
class NumberLookupTypeDaoHibernate
	extends HibernateDaoLegacy
	implements NumberLookupTypeDao {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	List <NumberLookupTypeRec> findAll (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findAll");

		) {

			return findMany (
				transaction,
				NumberLookupTypeRec.class,

				createCriteria (
					transaction,
					NumberLookupTypeRec.class)

			);

		}

	}

	@Override
	public
	NumberLookupTypeRec findRequired (
			@NonNull Transaction parentTransaction,
			@NonNull Long numberLookupTypeId) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findRequired");

		) {

			return get (
				transaction,
				NumberLookupTypeRec.class,
				numberLookupTypeId);

		}

	}

}
