package wbs.sms.message.core.model;

import java.util.List;

import org.joda.time.Instant;

public
interface MessageExpiryDaoMethods {

	List<MessageExpiryRec> findPendingLimit (
			Instant now,
			int maxResults);

}