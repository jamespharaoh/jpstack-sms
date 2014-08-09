package wbs.sms.number.list.hibernate;

import wbs.framework.hibernate.HibernateDao;
import wbs.sms.number.list.model.NumberListNumberDao;
import wbs.sms.number.list.model.NumberListNumberRec;

public
class NumberListNumberDaoHibernate
	extends HibernateDao
	implements NumberListNumberDao {

	@Override
	public
	NumberListNumberRec findByNumberListAndNumber (
			int numberListId,
			int numberId) {

		return findOne (
			NumberListNumberRec.class,

			createQuery (
				"FROM NumberListNumberRec numberListNumber " +
				"WHERE numberListNumber.numberList.id = :numberListId " +
					"AND numberListNumber.number.id = :numberId")

			.setInteger (
				"numberListId",
				numberListId)

			.setInteger (
				"numberId",
				numberId)

			.list ());

	}

}
