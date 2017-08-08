package wbs.utils.exception;

import lombok.NonNull;

public
class RuntimeSecurityException
	extends RuntimeException {

	public
	RuntimeSecurityException (
			@NonNull SecurityException cause) {

		super (
			cause);

	}

}
