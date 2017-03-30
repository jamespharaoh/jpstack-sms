package wbs.sms.number.list.hibernate;

import java.util.List;

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

	@Override
	public
	List <NumberListNumberRec> findManyPresent (
			@NonNull NumberListRec numberList,
			@NonNull List <NumberRec> numbers) {

		return findMany (
			"findMany (numberList, numbers)",
			NumberListNumberRec.class,

			createCriteria (
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
