package wbs.sms.message.inbox.daemon;

import javax.inject.Inject;

import lombok.NonNull;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.command.model.CommandRec;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.logic.InboxLogic;

@SingletonComponent ("nullCommandTypeHandler")
public
class NullCommandTypeHandler
	implements CommandHandler {

	@Inject
	CommandObjectHelper commandHelper;

	@Inject
	Database database;

	@Inject
	InboxLogic inboxLogic;

	@Inject
	MessageObjectHelper messageHelper;

	@Override
	public
	String[] getCommandTypes () {

		return new String [] {
			"root.null"
		};

	}

	@Override
	public
	Status handle (
			int commandId,
			@NonNull ReceivedMessage receivedMessage) {

		CommandRec command =
			commandHelper.find (
				commandId);

		MessageRec message =
			messageHelper.find (
				receivedMessage.getMessageId ());

		inboxLogic.inboxNotProcessed (
			message,
			null,
			null,
			command,
			"Null command handler");

		return null;

	}

}
