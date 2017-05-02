package wbs.sms.message.core.model;

import java.util.List;

import org.joda.time.Instant;

import wbs.framework.database.Transaction;

public
interface MessageExpiryDaoMethods {

	List <MessageExpiryRec> findPendingLimit (
			Transaction parentTransaction,
			Instant now,
			Long maxResults);

}