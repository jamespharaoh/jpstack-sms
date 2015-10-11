package wbs.sms.message.core.model;

import java.util.List;

public
interface MessageExpiryDaoMethods {

	List<MessageExpiryRec> findPendingLimit (
			int maxResults);

}