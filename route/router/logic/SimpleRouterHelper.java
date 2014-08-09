package wbs.sms.route.router.logic;

import javax.inject.Inject;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.object.ObjectManager;
import wbs.sms.route.core.model.RouteRec;
import wbs.sms.route.router.model.RouterRec;
import wbs.sms.route.router.model.SimpleRouterRec;

@SingletonComponent ("simpleRouterHelper")
public
class SimpleRouterHelper
	implements RouterHelper {

	// dependencies

	@Inject
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
			RouterRec router) {

		SimpleRouterRec simpleRouter =
			(SimpleRouterRec)
			(Object)
			objectManager.getParent (
				router);

		return simpleRouter.getRoute ();

	}

}
