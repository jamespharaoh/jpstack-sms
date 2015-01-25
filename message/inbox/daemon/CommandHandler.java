package wbs.sms.message.inbox.daemon;

import wbs.sms.command.model.CommandRec;
import wbs.sms.message.inbox.model.InboxAttemptRec;
import wbs.sms.message.inbox.model.InboxRec;

import com.google.common.base.Optional;

public
interface CommandHandler {

	String[] getCommandTypes ();

	CommandHandler inbox (
			InboxRec inbox);

	CommandHandler command (
			CommandRec command);

	CommandHandler commandRef (
			Optional<Integer> commandRef);

	CommandHandler rest (
			String rest);

	InboxAttemptRec handle ();

}
