package wbs.sms.route.router.hibernate;

import java.util.List;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.hibernate.HibernateDaoLegacy;
import wbs.framework.logging.LogContext;

import wbs.sms.route.router.model.RouterTypeDao;
import wbs.sms.route.router.model.RouterTypeRec;

@SingletonComponent ("routerTypeDao")
public
class RouterTypeDaoHibernate
	extends HibernateDaoLegacy
	implements RouterTypeDao {

	// singleton depedencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	List <RouterTypeRec> findAll (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findAll");

		) {

			return findMany (
				transaction,
				RouterTypeRec.class,

				createCriteria (
					transaction,
					RouterTypeRec.class)

			);

		}

	}

	@Override
	public
	RouterTypeRec findRequired (
			@NonNull Transaction parentTransaction,
			@NonNull Long routerTypeId) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findRequired");

		) {

			return get (
				transaction,
				RouterTypeRec.class,
				routerTypeId);

		}

	}

}
