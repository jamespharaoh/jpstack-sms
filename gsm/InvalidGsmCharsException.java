package wbs.sms.gsm;

public
class InvalidGsmCharsException
	extends Exception {

	public
	InvalidGsmCharsException () {
		super ();
	}

	public
	InvalidGsmCharsException (
			String message) {

		super (
			message);

	}

}
