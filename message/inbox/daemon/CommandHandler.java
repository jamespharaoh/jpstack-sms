package wbs.sms.message.inbox.daemon;

public
interface CommandHandler {

	// TODO should be an enum
	public final
	class Status {

		private
		Status () {
		}

		public final static
		Status processed =
			new Status ();

		public final static
		Status notprocessed =
			new Status ();

	}

	String[] getCommandTypes ();

	CommandHandler.Status handle (
			int commandId,
			ReceivedMessage receivedMessage);

}
