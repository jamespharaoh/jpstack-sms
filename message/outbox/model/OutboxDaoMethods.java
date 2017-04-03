package wbs.sms.message.outbox.model;

import java.util.List;
import java.util.Map;

import org.joda.time.Instant;

import wbs.platform.scaffold.model.SliceRec;

import wbs.sms.message.core.model.MessageRec;
import wbs.sms.route.core.model.RouteRec;

public
interface OutboxDaoMethods {

	Long count ();

	Long countOlderThan (
			SliceRec slice,
			Instant intant);

	OutboxRec find (
			MessageRec message);

	List <OutboxRec> findLimit (
			RouteRec route,
			Long maxResults);

	OutboxRec findNext (
			Instant now,
			RouteRec route);

	List <OutboxRec> findNextLimit (
			Instant now,
			RouteRec route,
			Long maxResults);

	List <OutboxRec> findSendingBeforeLimit (
			Instant sendingBefore,
			Long maxResults);

	Map <Long, Long> generateRouteSummary (
			Instant now);

}