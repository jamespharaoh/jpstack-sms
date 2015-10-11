package wbs.sms.route.http.model;

import wbs.sms.network.model.NetworkRec;
import wbs.sms.route.core.model.RouteRec;

public
interface HttpRouteDaoMethods {

	HttpRouteRec find (
			RouteRec route,
			NetworkRec network);

}