package wbs.sms.magicnumber.hibernate;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDaoLegacy;
import wbs.framework.logging.LogContext;

import wbs.sms.magicnumber.model.MagicNumberDao;
import wbs.sms.magicnumber.model.MagicNumberRec;
import wbs.sms.magicnumber.model.MagicNumberSetRec;
import wbs.sms.number.core.model.NumberRec;

public
class MagicNumberDaoHibernate
	extends HibernateDaoLegacy
	implements MagicNumberDao {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	MagicNumberRec findByNumber (
			@NonNull Transaction parentTransaction,
			@NonNull String number) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findByNumber");

		) {

			return findOneOrNull (
				transaction,
				MagicNumberRec.class,

				createCriteria (
					transaction,
					MagicNumberRec.class,
					"_magicNumber")

				.add (
					Restrictions.eq (
						"_magicNumber.number",
						number))

			);

		}

	}

	@Override
	public
	MagicNumberRec findExistingUnused (
			@NonNull Transaction parentTransaction,
			@NonNull MagicNumberSetRec magicNumberSet,
			@NonNull NumberRec number) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findExistingUnused");

		) {

			return findOne (
				MagicNumberRec.class,

				createQuery (
					transaction,
					"FROM MagicNumberRec magicNumber " +
					"WHERE magicNumber.magicNumberSet = :magicNumberSet " +
					"AND NOT EXISTS (" +
						"SELECT magicNumberUse.id " +
						"FROM magicNumber.magicNumberUses magicNumberUse " +
						"WHERE magicNumberUse.number = :number" +
					") " +
					"AND magicNumber.deleted = false")

				.setEntity (
					"magicNumberSet",
					magicNumberSet)

				.setEntity (
					"number",
					number)

				.list ());

		}

	}

}
