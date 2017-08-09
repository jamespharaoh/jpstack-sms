package wbs.sms.route.core.logic;

import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.route.core.model.RouteRec;

public
interface RouteLogic {

	/**
	 * Checks if a route supports sending or receiving of a specific message
	 * type.
	 *
	 * @param route
	 * @param messageTypeCode
	 * @param direction
	 * @return
	 */
	boolean checkRouteMessageType (
			RouteRec route,
			String messageTypeCode,
			MessageDirection direction);

}
