package wbs.sms.message.core.logic;

public
class InvalidMessageStateException
	extends RuntimeException {

	public
	InvalidMessageStateException () {
		super ();
	}

	public
	InvalidMessageStateException (
			String message) {

		super (
			message);

	}

}
