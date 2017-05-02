package wbs.sms.message.inbox.model;

import java.util.List;

import org.joda.time.Instant;

import wbs.framework.database.Transaction;

import wbs.sms.route.core.model.RouteRec;

public
interface InboxMultipartBufferDaoMethods {

	List <InboxMultipartBufferRec> findByOtherId (
			Transaction parentTransaction,
			RouteRec route,
			String otherId);

	List <InboxMultipartBufferRec> findRecent (
			Transaction parentTransaction,
			InboxMultipartBufferRec inboxMultipartBuffer,
			Instant timestamp);

}