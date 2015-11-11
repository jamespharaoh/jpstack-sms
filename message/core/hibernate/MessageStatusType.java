package wbs.sms.message.core.hibernate;

import java.sql.Types;

import wbs.framework.hibernate.EnumUserType;
import wbs.sms.message.core.model.MessageStatus;

public
class MessageStatusType
	extends EnumUserType<Integer,MessageStatus> {

	{

		sqlType (Types.INTEGER);
		enumClass (MessageStatus.class);

		add (0, MessageStatus.pending);
		add (1, MessageStatus.processed);
		add (2, MessageStatus.cancelled);
		add (3, MessageStatus.failed);
		add (4, MessageStatus.sent);
		add (5, MessageStatus.delivered);
		add (6, MessageStatus.undelivered);
		add (7, MessageStatus.notProcessed);
		add (8, MessageStatus.ignored);
		add (9, MessageStatus.manuallyProcessed);
		add (10, MessageStatus.submitted);
		add (11, MessageStatus.reportTimedOut);
		add (12, MessageStatus.held);
		add (13, MessageStatus.blacklisted);
		add (14, MessageStatus.manuallyUndelivered);
		add (15, MessageStatus.manuallyDelivered);

	}

}
