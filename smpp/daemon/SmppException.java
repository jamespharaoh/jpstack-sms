package wbs.sms.smpp.daemon;

public
class SmppException
	extends RuntimeException {

	private
	Integer commandStatus;

	public
	SmppException (
			String message) {

		super (
			message);

	}

	public
	SmppException (
			int newCommandStatus) {

		super (
			"SMPP error " + newCommandStatus);

		commandStatus =
			newCommandStatus;

	}

	public
	Integer getCommandStatus () {
		return commandStatus;
	}

}
