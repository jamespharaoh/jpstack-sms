package wbs.sms.gsm;

public
class PduDecodeException
	extends RuntimeException {

	public
	PduDecodeException() {
		super ();
	}

	public
	PduDecodeException (
			String message) {

		super (
			message);

	}

}
