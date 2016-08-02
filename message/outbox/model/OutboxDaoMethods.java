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
			long maxResults);

	OutboxRec findNext (
			Instant now,
			RouteRec route);

	List<OutboxRec> findNextLimit (
			Instant now,
			RouteRec route,
			long maxResults);

	List<OutboxRec> findSendingBeforeLimit (
			Instant sendingBefore,
			long maxResults);

	Map<Long,Long> generateRouteSummary (
			Instant now);

}