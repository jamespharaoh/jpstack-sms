package wbs.sms.route.router.logic;

import static wbs.utils.etc.TypeUtils.genericCastUnchecked;

import lombok.NonNull;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.object.ObjectManager;

import wbs.sms.route.core.model.RouteRec;
import wbs.sms.route.router.model.RouterRec;
import wbs.sms.route.router.model.SimpleRouterRec;

@SingletonComponent ("simpleRouterHelper")
public
class SimpleRouterHelper
	implements RouterHelper {

	// singleton dependencies

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
			@NonNull RouterRec router) {

		SimpleRouterRec simpleRouter =
			genericCastUnchecked (
				objectManager.getParentRequired (
					router));

		return simpleRouter.getRoute ();

	}

}
