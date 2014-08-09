package wbs.sms.core.logic;

public
class NoSuchMessageException
	extends RuntimeException {

	public
	NoSuchMessageException () {

		super ();

	}

	public
	NoSuchMessageException (
			String message) {

		super (
			message);

	}

}
