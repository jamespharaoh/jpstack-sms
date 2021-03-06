package wbs.sms.route.router.logic;

import wbs.framework.database.Transaction;

import wbs.sms.route.core.model.RouteRec;
import wbs.sms.route.router.model.RouterRec;

public
interface RouterLogic {

	RouteRec resolveRouter (
			Transaction parentTransaction,
			RouterRec router);

}
