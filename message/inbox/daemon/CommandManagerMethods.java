package wbs.sms.message.inbox.daemon;

import com.google.common.base.Optional;

import wbs.sms.command.model.CommandRec;
import wbs.sms.message.inbox.model.InboxAttemptRec;
import wbs.sms.message.inbox.model.InboxRec;

public
interface CommandManagerMethods {

	InboxAttemptRec handle (
			InboxRec inbox,
			CommandRec command,
			Optional<Integer> ref,
			String rest);

}
