package wbs.utils.exception;

import lombok.NonNull;

public
class RuntimeNoSuchMethodException
	extends RuntimeReflectiveOperationException {

	public
	RuntimeNoSuchMethodException (
			@NonNull NoSuchMethodException cause) {

		super (
			cause);

	}

	public
	RuntimeNoSuchMethodException (
			@NonNull String message) {

		super (
			message);

	}

}
