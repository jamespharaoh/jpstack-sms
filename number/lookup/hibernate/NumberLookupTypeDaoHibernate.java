package wbs.sms.number.lookup.hibernate;

import java.util.List;

import lombok.NonNull;

import wbs.framework.hibernate.HibernateDao;
import wbs.sms.number.lookup.model.NumberLookupTypeDao;
import wbs.sms.number.lookup.model.NumberLookupTypeRec;

public
class NumberLookupTypeDaoHibernate
	extends HibernateDao
	implements NumberLookupTypeDao {

	@Override
	public
	List<NumberLookupTypeRec> findAll () {

		return findMany (
			"findAll ()",
			NumberLookupTypeRec.class,

			createCriteria (
				NumberLookupTypeRec.class)

		);

	}

	@Override
	public
	NumberLookupTypeRec findRequired (
			@NonNull Long numberLookupTypeId) {

		return get (
			NumberLookupTypeRec.class,
			(int) (long) numberLookupTypeId);

	}

}
