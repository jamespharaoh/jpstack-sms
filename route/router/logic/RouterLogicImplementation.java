package wbs.sms.route.router.logic;

import javax.inject.Inject;

import lombok.NonNull;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.sms.route.core.model.RouteRec;
import wbs.sms.route.router.model.RouterRec;
import wbs.sms.route.router.model.RouterTypeRec;

@SingletonComponent ("routerLogic")
public
class RouterLogicImplementation
	implements RouterLogic {

	// dependencies

	@Inject
	RouterHelperManager routerHelperManager;

	// implementation

	@Override
	public
	RouteRec resolveRouter (
			@NonNull RouterRec router) {

		RouterTypeRec routerType =
			router.getRouterType ();

		RouterHelper routerHelper =
			routerHelperManager.forParentObjectTypeCode (
				routerType.getParentType ().getCode (),
				true);

		return routerHelper.resolve (
			router);

	}

}
