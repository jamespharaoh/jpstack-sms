package wbs.sms.message.inbox.model;

import java.util.List;

import org.joda.time.Instant;

import wbs.framework.database.Transaction;

public
interface InboxMultipartLogDaoMethods {

	List <InboxMultipartLogRec> findRecent (
			Transaction parentTransaction,
			InboxMultipartBufferRec inboxMultipartBuffer,
			Instant timestamp);

}