package wbs.sms.locator.logic;

public
class LocatorException
	extends RuntimeException {

	private static final
	long serialVersionUID =
		7627492824541488500L;

	private final
	String errorCode;

	public
	LocatorException () {

		super ();

		errorCode = null;

	}

	public
	LocatorException (
			String message) {

		super (message);

		errorCode = null;

	}

	public
	LocatorException (
			Throwable cause) {

		super (cause);

		errorCode = null;

	}

	public
	LocatorException (
			String message,
			Throwable cause) {

		super (
			message,
			cause);

		errorCode = null;

	}

	public
	LocatorException (
			String message,
			String newErrorCode) {

		super (message);

		errorCode =
			newErrorCode;

	}

	public
	String getErrorCode () {
		return errorCode;
	}

}
