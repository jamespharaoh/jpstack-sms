package wbs.sms.message.inbox.daemon;

public 
interface CommandManagerMethods {

	void handle (
		int commandId,
		ReceivedMessage message);

	void handle (
		int commandId,
		ReceivedMessage message,
		String rest);

}
