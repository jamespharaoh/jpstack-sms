package wbs.sms.route.http.model;

import wbs.framework.database.Transaction;

import wbs.sms.network.model.NetworkRec;
import wbs.sms.route.core.model.RouteRec;

public
interface HttpRouteDaoMethods {

	HttpRouteRec find (
			Transaction parentTransaction,
			RouteRec route,
			NetworkRec network);

}