package wbs.sms.message.inbox.model;

import java.util.List;

import org.joda.time.Instant;

public
interface InboxDaoMethods {

	Long countPending ();

	List <InboxRec> findPendingLimit (
			Instant now,
			Long maxResults);

	List <InboxRec> findPendingLimit (
			Long maxResults);

}