package wbs.sms.gsm;

import lombok.NonNull;

public
class InvalidGsmCharsException
	extends IllegalArgumentException {

	public
	InvalidGsmCharsException () {
		super ();
	}

	public
	InvalidGsmCharsException (
			@NonNull String message) {

		super (
			message);

	}

}
