package wbs.sms.magicnumber.hibernate;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;

import wbs.framework.hibernate.HibernateDao;
import wbs.sms.magicnumber.model.MagicNumberDao;
import wbs.sms.magicnumber.model.MagicNumberRec;
import wbs.sms.magicnumber.model.MagicNumberSetRec;
import wbs.sms.number.core.model.NumberRec;

public
class MagicNumberDaoHibernate
	extends HibernateDao
	implements MagicNumberDao {

	@Override
	public
	MagicNumberRec findByNumber (
			@NonNull String number) {

		return findOne (
			"findByNumber (number)",
			MagicNumberRec.class,

			createCriteria (
				MagicNumberRec.class,
				"_magicNumber")

			.add (
				Restrictions.eq (
					"_magicNumber.number",
					number))

		);

	}

	@Override
	public
	MagicNumberRec findExistingUnused (
			@NonNull MagicNumberSetRec magicNumberSet,
			@NonNull NumberRec number) {

		return findOne (
			MagicNumberRec.class,

			createQuery (
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
