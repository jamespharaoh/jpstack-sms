package wbs.sms.message.outbox.model;

import java.util.List;
import java.util.Map;

import org.joda.time.Instant;

import wbs.sms.message.core.model.MessageRec;
import wbs.sms.route.core.model.RouteRec;

public
interface OutboxDaoMethods {

	int count ();

	OutboxRec find (
			MessageRec message);

	List<OutboxRec> findLimit (
			RouteRec route,
			int maxResults);

	OutboxRec findNext (
			RouteRec route);

	List<OutboxRec> findNextLimit (
			RouteRec route,
			int maxResults);

	List<OutboxRec> findSendingBeforeLimit (
			Instant sendingBefore,
			int maxResults);

	Map<Integer,Integer> generateRouteSummary ();

}