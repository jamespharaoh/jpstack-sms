package wbs.sms.route.core.console;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.platform.console.helper.EnumConsoleHelper;
import wbs.sms.route.core.model.RouteNetworkBehaviour;

@SingletonComponent ("routeNetworkBehaviourConsoleHelper")
public
class RouteNetworkBehaviourConsoleHelper
	extends EnumConsoleHelper<RouteNetworkBehaviour> {

	{

		enumClass (RouteNetworkBehaviour.class);

		auto ();

	}

}
