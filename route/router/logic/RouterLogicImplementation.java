package wbs.sms.route.router.logic;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.sms.route.core.model.RouteRec;
import wbs.sms.route.router.model.RouterRec;
import wbs.sms.route.router.model.RouterTypeRec;

@SingletonComponent ("routerLogic")
public
class RouterLogicImplementation
	implements RouterLogic {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RouterHelperManager routerHelperManager;

	// implementation

	@Override
	public
	RouteRec resolveRouter (
			@NonNull Transaction parentTransaction,
			@NonNull RouterRec router) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"resolveRouter");

		) {

			RouterTypeRec routerType =
				router.getRouterType ();

			RouterHelper routerHelper =
				routerHelperManager.forParentObjectTypeCode (
					routerType.getParentType ().getCode (),
					true);

			return routerHelper.resolve (
				transaction,
				router);

		}

	}

}
