package wbs.sms.route.router.logic;

import static wbs.utils.etc.TypeUtils.genericCastUnchecked;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.object.ObjectManager;

import wbs.sms.route.core.model.RouteRec;
import wbs.sms.route.router.model.RouterRec;

@SingletonComponent ("staticRouterHelper")
public
class StaticRouterHelper
	implements RouterHelper {

	// singleton dependencies

	@SingletonDependency
	ObjectManager objectManager;

	// details

	@Override
	public
	String routerTypeCode () {
		return "route";
	}

	// implementation

	@Override
	public
	RouteRec resolve (
			RouterRec router) {

		RouteRec route =
			genericCastUnchecked (
				objectManager.getParentRequired (
					router));

		return route;

	}

}
