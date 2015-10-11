package wbs.sms.message.inbox.model;

import java.util.Date;
import java.util.List;

import wbs.sms.route.core.model.RouteRec;

public
interface InboxMultipartBufferDaoMethods {

	List<InboxMultipartBufferRec> findByOtherId (
			RouteRec route,
			String otherId);

	List<InboxMultipartBufferRec> findRecent (
			InboxMultipartBufferRec inboxMultipartBuffer,
			Date timestamp);

}