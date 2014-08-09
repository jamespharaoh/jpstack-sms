package wbs.sms.route.router.hibernate;

import java.util.List;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.hibernate.HibernateDao;
import wbs.platform.object.core.model.ObjectTypeRec;
import wbs.sms.route.router.model.RouterDao;
import wbs.sms.route.router.model.RouterTypeRec;

@SingletonComponent ("routerDao")
public
class RouterDaoHibernate
	extends HibernateDao
	implements RouterDao {

	@Override
	public
	List<RouterTypeRec> findByParentObjectType (
			ObjectTypeRec parentObjectType) {

		return findMany (
			RouterTypeRec.class,

			createQuery (
				"FROM RouterTypeRec routerType " +
				"WHERE routerType.parentObjectType = :parentObjectType")

			.setEntity (
				"parentObjectType",
				parentObjectType)

			.list ());

	}

}
