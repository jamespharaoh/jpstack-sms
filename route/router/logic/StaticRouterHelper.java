package wbs.sms.route.router.logic;

import javax.inject.Inject;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.object.ObjectManager;
import wbs.sms.route.core.model.RouteRec;
import wbs.sms.route.router.model.RouterRec;

@SingletonComponent ("staticRouterHelper")
public
class StaticRouterHelper
	implements RouterHelper {

	// dependencies

	@Inject
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
			(RouteRec)
			objectManager.getParent (
				router);

		return route;

	}

}
