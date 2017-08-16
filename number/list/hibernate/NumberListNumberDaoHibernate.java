package wbs.sms.number.list.hibernate;

import static wbs.utils.etc.NullUtils.isNotNull;

import java.util.List;

import lombok.NonNull;

import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDaoLegacy;
import wbs.framework.logging.LogContext;

import wbs.sms.number.core.model.NumberRec;
import wbs.sms.number.list.model.NumberListNumberDao;
import wbs.sms.number.list.model.NumberListNumberRec;
import wbs.sms.number.list.model.NumberListNumberSearch;
import wbs.sms.number.list.model.NumberListRec;

public
class NumberListNumberDaoHibernate
	extends HibernateDaoLegacy
	implements NumberListNumberDao {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	NumberListNumberRec find (
			@NonNull Transaction parentTransaction,
			@NonNull NumberListRec numberList,
			@NonNull NumberRec number) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"find");

		) {

			return findOneOrNull (
				transaction,
				NumberListNumberRec.class,

				createCriteria (
					transaction,
					NumberListNumberRec.class,
					"_numberListNumber")

				.add (
					Restrictions.eq (
						"_numberListNumber.numberList",
						numberList))

				.add (
					Restrictions.eq (
						"_numberListNumber.number",
						number))

			);

		}

	}

	@Override
	public
	List <NumberListNumberRec> findManyPresent (
			@NonNull Transaction parentTransaction,
			@NonNull NumberListRec numberList,
			@NonNull List <NumberRec> numbers) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findManyPresent");

		) {

			return findMany (
				transaction,
				NumberListNumberRec.class,

				createCriteria (
					transaction,
					NumberListNumberRec.class,
					"_numberListNumber")

				.add (
					Restrictions.eq (
						"_numberListNumber.numberList",
						numberList))

				.add (
					Restrictions.in (
						"_numberListNumber.number",
						numbers))

				.add (
					Restrictions.eq (
						"_numberListNumber.present",
						true))

			);

		}

	}

	@Override
	public
	List <Long> searchIds (
			@NonNull Transaction parentTransaction,
			@NonNull NumberListNumberSearch search) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"searchIds");

		) {

			Criteria criteria =
				createCriteria (
					transaction,
					NumberListNumberRec.class,
					"_numberListNumber")

				.createAlias (
					"_numberListNumber.numberList",
					"_numberList")

			;


			if (
				isNotNull (
					search.numberListId ())
			) {

				criteria.add (
					Restrictions.eq (
						"_numberList.id",
						search.numberListId ()));

			}

			if (
				isNotNull (
					search.present ())
			) {

				criteria.add (
					Restrictions.eq (
						"_numberListNumber.present",
						search.present ()));

			}

			criteria.setProjection (
				Projections.id ());

			return findMany (
				transaction,
				Long.class,
				criteria);

		}

	}

}
