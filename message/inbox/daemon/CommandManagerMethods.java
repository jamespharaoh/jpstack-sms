package wbs.sms.message.inbox.daemon;

import wbs.sms.message.inbox.daemon.CommandHandler.Status;

public interface CommandManagerMethods {

	Status handle (
		int commandId,
		ReceivedMessage message);

	Status handle (
		int commandId,
		ReceivedMessage message,
		String rest);

}
