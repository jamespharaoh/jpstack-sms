package wbs.sms.route.router.hibernate;

import java.util.List;

import lombok.NonNull;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.hibernate.HibernateDao;
import wbs.sms.route.router.model.RouterTypeDao;
import wbs.sms.route.router.model.RouterTypeRec;

@SingletonComponent ("routerTypeDao")
public
class RouterTypeDaoHibernate
	extends HibernateDao
	implements RouterTypeDao {

	@Override
	public
	List<RouterTypeRec> findAll () {

		return findMany (
			"findAll ()",
			RouterTypeRec.class,

			createCriteria (
				RouterTypeRec.class)

		);

	}

	@Override
	public
	RouterTypeRec findRequired (
			@NonNull Long routerTypeId) {

		return get (
			RouterTypeRec.class,
			(int) (long) routerTypeId);

	}

}
