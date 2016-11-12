package wbs.sms.message.inbox.daemon;

import com.google.common.base.Optional;

import wbs.framework.logging.TaskLogger;
import wbs.sms.command.model.CommandRec;
import wbs.sms.message.inbox.model.InboxAttemptRec;
import wbs.sms.message.inbox.model.InboxRec;

public
interface CommandHandler {

	String[] getCommandTypes ();

	CommandHandler inbox (
			InboxRec inbox);

	CommandHandler command (
			CommandRec command);

	CommandHandler commandRef (
			Optional<Long> commandRef);

	CommandHandler rest (
			String rest);

	InboxAttemptRec handle (
			TaskLogger taskLogger);

}
