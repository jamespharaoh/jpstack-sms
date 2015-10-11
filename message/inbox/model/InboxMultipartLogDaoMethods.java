package wbs.sms.message.inbox.model;

import java.util.Date;
import java.util.List;

public
interface InboxMultipartLogDaoMethods {

	List<InboxMultipartLogRec> findRecent (
			InboxMultipartBufferRec inboxMultipartBuffer,
			Date timestamp);

}