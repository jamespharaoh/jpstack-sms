package wbs.utils.exception;

import lombok.NonNull;

public
class RuntimeReflectiveOperationException
	extends RuntimeException {

	public
	RuntimeReflectiveOperationException (
			@NonNull ReflectiveOperationException cause) {

		super (
			cause);

	}

	public
	RuntimeReflectiveOperationException (
			@NonNull String message) {

		super (
			message);

	}

}
