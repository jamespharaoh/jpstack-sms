package wbs.sms.message.inbox.model;

import java.util.List;

import org.joda.time.Instant;

import wbs.platform.scaffold.model.SliceRec;

public
interface InboxDaoMethods {

	Long countPending ();

	Long countPendingOlderThan (
			SliceRec slice,
			Instant instant);

	List <InboxRec> findPendingLimit (
			Instant now,
			Long maxResults);

	List <InboxRec> findPendingLimit (
			Long maxResults);

}