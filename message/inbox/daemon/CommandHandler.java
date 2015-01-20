package wbs.sms.message.inbox.daemon;

public
interface CommandHandler {

	String[] getCommandTypes ();

	void handle (
			int commandId,
			ReceivedMessage receivedMessage);

}
