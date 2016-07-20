package wbs.sms.message.inbox.model;

import java.util.List;

import org.joda.time.Instant;

public
interface InboxMultipartLogDaoMethods {

	List<InboxMultipartLogRec> findRecent (
			InboxMultipartBufferRec inboxMultipartBuffer,
			Instant timestamp);

}