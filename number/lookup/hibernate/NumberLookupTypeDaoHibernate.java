package wbs.sms.number.lookup.hibernate;

import java.util.List;

import org.hibernate.criterion.Restrictions;

import wbs.framework.hibernate.HibernateDao;
import wbs.platform.object.core.model.ObjectTypeRec;
import wbs.sms.number.lookup.model.NumberLookupTypeDao;
import wbs.sms.number.lookup.model.NumberLookupTypeRec;

public
class NumberLookupTypeDaoHibernate
	extends HibernateDao
	implements NumberLookupTypeDao {

	@Override
	public
	List<NumberLookupTypeRec> findByParentObjectType (
			ObjectTypeRec parentObjectType) {

		return findMany (
			NumberLookupTypeRec.class,

			createCriteria (
				NumberLookupTypeRec.class,
				"_numberLookupType")

			.add (
				Restrictions.eq (
					"_numberLookupType.parentObjectType",
					parentObjectType))

			.list ());

	}

}
