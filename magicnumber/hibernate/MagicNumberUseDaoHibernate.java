package wbs.sms.magicnumber.hibernate;

import lombok.NonNull;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDaoLegacy;
import wbs.framework.logging.LogContext;

import wbs.sms.command.model.CommandRec;
import wbs.sms.magicnumber.model.MagicNumberRec;
import wbs.sms.magicnumber.model.MagicNumberSetRec;
import wbs.sms.magicnumber.model.MagicNumberUseDao;
import wbs.sms.magicnumber.model.MagicNumberUseRec;
import wbs.sms.number.core.model.NumberRec;

public
class MagicNumberUseDaoHibernate
	extends HibernateDaoLegacy
	implements MagicNumberUseDao {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	MagicNumberUseRec find (
			@NonNull Transaction parentTransaction,
			@NonNull MagicNumberRec magicNumber,
			@NonNull NumberRec number) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"find");

		) {

			return findOneOrNull (
				transaction,
				MagicNumberUseRec.class,

				createCriteria (
					transaction,
					MagicNumberUseRec.class,
					"_magicNumberUse")

				.add (
					Restrictions.eq (
						"_magicNumberUse.magicNumber",
						magicNumber))

				.add (
					Restrictions.eq (
						"_magicNumberUse.number",
						number))

			);

		}

	}

	@Override
	public
	MagicNumberUseRec findExistingByRef (
			@NonNull Transaction parentTransaction,
			@NonNull MagicNumberSetRec magicNumberSet,
			@NonNull NumberRec number,
			@NonNull CommandRec command,
			@NonNull Long ref) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findExistingByRef");

		) {

			return findOneOrNull (
				transaction,
				MagicNumberUseRec.class,

				createCriteria (
					transaction,
					MagicNumberUseRec.class,
					"_magicNumberUse")

				.createAlias (
					"_magicNumberUse.magicNumber",
					"_magicNumber")

				.add (
					Restrictions.eq (
						"_magicNumber.magicNumberSet",
						magicNumberSet))

				.add (
					Restrictions.eq (
						"_magicNumberUse.number",
						number))

				.add (
					Restrictions.eq (
						"_magicNumberUse.command",
						command))

				.add (
					Restrictions.eq (
						"_magicNumberUse.refId",
						ref))

				.add (
					Restrictions.eq (
						"_magicNumber.deleted",
						false))

			);

		}

	}

	@Override
	public
	MagicNumberUseRec findExistingLeastRecentlyUsed (
			@NonNull Transaction parentTransaction,
			@NonNull MagicNumberSetRec magicNumberSet,
			@NonNull NumberRec number) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findExistingLeastRecentlyUsed");

		) {

			return findOneOrNull (
				transaction,
				MagicNumberUseRec.class,

				createCriteria (
					transaction,
					MagicNumberUseRec.class,
					"_magicNumberUse")

				.createAlias (
					"_magicNumberUse.magicNumber",
					"_magicNumber")

				.add (
					Restrictions.eq (
						"_magicNumber.magicNumberSet",
						magicNumberSet))

				.add (
					Restrictions.eq (
						"_magicNumberUse.number",
						number))

				.add (
					Restrictions.eq (
						"_magicNumber.deleted",
						false))

				.addOrder (
					Order.asc (
						"_magicNumberUse.lastUseTimestamp"))

				.setMaxResults (1)

			);

		}

	}

}
