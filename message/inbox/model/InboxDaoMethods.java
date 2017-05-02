package wbs.sms.message.inbox.model;

import java.util.List;

import org.joda.time.Instant;

import wbs.framework.database.Transaction;

import wbs.platform.scaffold.model.SliceRec;

import wbs.sms.route.core.model.RouteRec;

public
interface InboxDaoMethods {

	Long countPending (
			Transaction parentTransaction);

	Long countPendingOlderThan (
			Transaction parentTransaction,
			SliceRec slice,
			Instant instant);

	Long countPendingOlderThan (
			Transaction parentTransaction,
			RouteRec route,
			Instant instant);

	List <InboxRec> findPendingLimit (
			Transaction parentTransaction,
			Instant now,
			Long maxResults);

	List <InboxRec> findPendingLimit (
			Transaction parentTransaction,
			Long maxResults);

}