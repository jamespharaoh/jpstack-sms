package wbs.sms.message.outbox.model;

import java.util.List;
import java.util.Map;

import org.joda.time.Instant;

import wbs.framework.database.Transaction;

import wbs.platform.scaffold.model.SliceRec;

import wbs.sms.message.core.model.MessageRec;
import wbs.sms.route.core.model.RouteRec;

public
interface OutboxDaoMethods {

	Long count (
			Transaction parentTransaction);

	Long countOlderThan (
			Transaction parentTransaction,
			SliceRec slice,
			Instant intant);

	OutboxRec find (
			Transaction parentTransaction,
			MessageRec message);

	List <OutboxRec> findLimit (
			Transaction parentTransaction,
			RouteRec route,
			Long maxResults);

	OutboxRec findNext (
			Transaction parentTransaction,
			Instant now,
			RouteRec route);

	List <OutboxRec> findNextLimit (
			Transaction parentTransaction,
			Instant now,
			RouteRec route,
			Long maxResults);

	List <OutboxRec> findSendingBeforeLimit (
			Transaction parentTransaction,
			Instant sendingBefore,
			Long maxResults);

	Map <Long, Long> generateRouteSummary (
			Transaction parentTransaction,
			Instant now);

}