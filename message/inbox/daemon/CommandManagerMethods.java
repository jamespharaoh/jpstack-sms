package wbs.sms.message.inbox.daemon;

import wbs.sms.command.model.CommandRec;
import wbs.sms.message.inbox.model.InboxAttemptRec;
import wbs.sms.message.inbox.model.InboxRec;

import com.google.common.base.Optional;

public
interface CommandManagerMethods {

	InboxAttemptRec handle (
			InboxRec inbox,
			CommandRec command,
			Optional<Integer> ref,
			String rest);

}
