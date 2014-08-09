package wbs.sms.magicnumber.hibernate;

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
			String number) {

		return findOne (
			MagicNumberRec.class,

			createQuery (
				"FROM MagicNumberRec magicNumber " +
				"WHERE magicNumber.number = :number")

			.setString (
				"number",
				number)

			.list ());

	}

	@Override
	public
	MagicNumberRec findUnused (
			MagicNumberSetRec magicNumberSet,
			NumberRec number) {

		return findOne (
			MagicNumberRec.class,

			createQuery (
				"FROM MagicNumberRec magicNumber " +
				"WHERE magicNumber.magicNumberSet = :magicNumberSet " +
					"AND NOT EXISTS (" +
						"SELECT magicNumberUse.id " +
						"FROM magicNumber.magicNumberUses magicNumberUse " +
						"WHERE magicNumberUse.number = :number" +
					")")

			.setEntity (
				"magicNumberSet",
				magicNumberSet)

			.setEntity (
				"number",
				number)

			.list ());

	}

}
