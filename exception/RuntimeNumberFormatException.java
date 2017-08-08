package wbs.utils.exception;

import lombok.NonNull;

public
class RuntimeNumberFormatException
	extends RuntimeException {

	public
	RuntimeNumberFormatException (
			@NonNull NumberFormatException cause) {

		super (
			cause);

	}

}
