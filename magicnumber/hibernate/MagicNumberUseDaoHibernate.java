package wbs.sms.magicnumber.hibernate;

import lombok.NonNull;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import wbs.framework.hibernate.HibernateDao;
import wbs.sms.command.model.CommandRec;
import wbs.sms.magicnumber.model.MagicNumberRec;
import wbs.sms.magicnumber.model.MagicNumberSetRec;
import wbs.sms.magicnumber.model.MagicNumberUseDao;
import wbs.sms.magicnumber.model.MagicNumberUseRec;
import wbs.sms.number.core.model.NumberRec;

public
class MagicNumberUseDaoHibernate
	extends HibernateDao
	implements MagicNumberUseDao {

	@Override
	public
	MagicNumberUseRec find (
			@NonNull MagicNumberRec magicNumber,
			@NonNull NumberRec number) {

		return findOne (
			"find (magicNumber, number)",
			MagicNumberUseRec.class,

			createCriteria (
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

	@Override
	public
	MagicNumberUseRec findExistingByRef (
			@NonNull MagicNumberSetRec magicNumberSet,
			@NonNull NumberRec number,
			@NonNull CommandRec command,
			@NonNull Long ref) {

		return findOne (
			"findExistingByRef (magicNumberSet, number, command, ref)",
			MagicNumberUseRec.class,

			createCriteria (
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

	@Override
	public
	MagicNumberUseRec findExistingLeastRecentlyUsed (
			@NonNull MagicNumberSetRec magicNumberSet,
			@NonNull NumberRec number) {

		return findOne (
			"findExistingLeastRecentlyUsed (magicNumberSet, number)",
			MagicNumberUseRec.class,

			createCriteria (
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
