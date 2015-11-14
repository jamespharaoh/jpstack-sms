package wbs.sms.route.router.hibernate;

import java.util.List;

import lombok.NonNull;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.hibernate.HibernateDao;
import wbs.platform.object.core.model.ObjectTypeRec;
import wbs.sms.route.router.model.RouterTypeDao;
import wbs.sms.route.router.model.RouterTypeRec;

@SingletonComponent ("routerTypeDao")
public
class RouterTypeDaoHibernate
	extends HibernateDao
	implements RouterTypeDao {

	@Override
	public
	List<RouterTypeRec> findByParentType (
			@NonNull ObjectTypeRec parentType) {

		return findMany (
			RouterTypeRec.class,

			createQuery (
				"FROM RouterTypeRec routerType " +
				"WHERE routerType.parentType = :parentType")

			.setEntity (
				"parentType",
				parentType)

			.list ());

	}

}
