package wbs.sms.message.inbox.model;

import java.util.List;

import org.joda.time.Instant;

import wbs.sms.route.core.model.RouteRec;

public
interface InboxMultipartBufferDaoMethods {

	List<InboxMultipartBufferRec> findByOtherId (
			RouteRec route,
			String otherId);

	List<InboxMultipartBufferRec> findRecent (
			InboxMultipartBufferRec inboxMultipartBuffer,
			Instant timestamp);

}