package wbs.sms.number.format.logic;

public
class WbsNumberFormatException
	extends Exception {

	private static final
	long serialVersionUID = -2790390976587564540L;

	public
	WbsNumberFormatException () {
		super ();
	}

	public
	WbsNumberFormatException (
			String message) {
		super (message);
	}

	public
	WbsNumberFormatException (
			Throwable cause) {
		super (cause);
	}

	public
	WbsNumberFormatException (
			String message,
			Throwable cause) {

		super (
			message,
			cause);

	}
}
