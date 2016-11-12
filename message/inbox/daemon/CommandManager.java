package wbs.sms.message.inbox.daemon;

import com.google.common.base.Optional;

import wbs.framework.logging.TaskLogger;
import wbs.sms.command.model.CommandRec;
import wbs.sms.message.inbox.model.InboxAttemptRec;
import wbs.sms.message.inbox.model.InboxRec;

public
interface CommandManager {

	InboxAttemptRec handle (
			TaskLogger taskLogger,
			InboxRec inbox,
			CommandRec command,
			Optional<Long> ref,
			String rest);

}