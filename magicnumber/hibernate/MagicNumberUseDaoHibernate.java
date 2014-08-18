package wbs.sms.magicnumber.hibernate;

import lombok.NonNull;
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
			MagicNumberRec magicNumber,
			NumberRec number) {

		return findOne (
			MagicNumberUseRec.class,

			createQuery (
				"FROM MagicNumberUseRec magicNumberUse " +
				"WHERE magicNumberUse.magicNumber = :magicNumber " +
					"AND magicNumberUse.number = :number")

			.setEntity (
				"magicNumber",
				magicNumber)

			.setEntity (
				"number",
				number)

			.list ());

	}

	@Override
	public
	MagicNumberUseRec findExistingByRef (
			@NonNull MagicNumberSetRec magicNumberSet,
			@NonNull NumberRec number,
			@NonNull CommandRec command,
			@NonNull Integer ref) {

		return findOne (
			MagicNumberUseRec.class,

			createQuery (
				"SELECT magicNumberUse " +
				"FROM MagicNumberUseRec magicNumberUse " +
					"INNER JOIN magicNumberUse.magicNumber magicNumber " +
				"WHERE magicNumber.magicNumberSet = :magicNumberSet " +
					"AND magicNumberUse.number= :number " +
					"AND magicNumberUse.command= :command " +
					"AND magicNumberUse.refId = :ref " +
					"AND magicNumber.deleted = false")

			.setEntity (
				"magicNumberSet",
				magicNumberSet)

			.setEntity (
				"number",
				number)

			.setEntity (
				"command",
				command)

			.setInteger (
				"ref",
				ref)

			.list ());

	}

	@Override
	public
	MagicNumberUseRec findExistingLeastRecentlyUsed (
			MagicNumberSetRec magicNumberSet,
			NumberRec number) {

		return findOne (
			MagicNumberUseRec.class,

			createQuery (
				"FROM MagicNumberUseRec magicNumberUse " +
				"WHERE magicNumberUse.magicNumber.magicNumberSet = :magicNumberSet " +
					"AND magicNumberUse.number = :number " +
					"AND magicNumberUse.magicNumber.deleted = false " +
				"ORDER BY magicNumberUse.lastUseTimestamp")

			.setEntity (
				"magicNumberSet",
				magicNumberSet)

			.setEntity (
				"number",
				number)

			.setMaxResults (1)

			.list ());

	}

}
