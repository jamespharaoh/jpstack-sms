package wbs.sms.number.list.hibernate;

import lombok.NonNull;

import org.hibernate.criterion.Restrictions;

import wbs.framework.hibernate.HibernateDao;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.number.list.model.NumberListNumberDao;
import wbs.sms.number.list.model.NumberListNumberRec;
import wbs.sms.number.list.model.NumberListRec;

public
class NumberListNumberDaoHibernate
	extends HibernateDao
	implements NumberListNumberDao {

	@Override
	public
	NumberListNumberRec find (
			@NonNull NumberListRec numberList,
			@NonNull NumberRec number) {

		return findOneOrNull (
			"find (numberList, number)",
			NumberListNumberRec.class,

			createCriteria (
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
