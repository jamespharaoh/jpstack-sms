package wbs.sms.message.inbox.model;

import java.util.List;

import org.joda.time.Instant;

public
interface InboxDaoMethods {

	int countPending ();

	List<InboxRec> findPendingLimit (
			Instant now,
			int maxResults);

	List<InboxRec> findPendingLimit (
			int maxResults);

}