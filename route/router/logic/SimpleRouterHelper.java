package wbs.sms.route.router.logic;

import static wbs.utils.etc.TypeUtils.genericCastUnchecked;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.object.ObjectManager;

import wbs.sms.route.core.model.RouteRec;
import wbs.sms.route.router.model.RouterRec;
import wbs.sms.route.router.model.SimpleRouterRec;

@SingletonComponent ("simpleRouterHelper")
public
class SimpleRouterHelper
	implements RouterHelper {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ObjectManager objectManager;

	// details

	@Override
	public
	String routerTypeCode () {
		return "simple_router";
	}

	// implementation

	@Override
	public
	RouteRec resolve (
			@NonNull Transaction parentTransaction,
			@NonNull RouterRec router) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"resolve");

		) {

			SimpleRouterRec simpleRouter =
				genericCastUnchecked (
					objectManager.getParentRequired (
						transaction,
						router));

			return simpleRouter.getRoute ();

		}

	}

}
