package wbs.utils.exception;

import lombok.NonNull;

public
class RuntimeIllegalAccessException
	extends RuntimeReflectiveOperationException {

	public
	RuntimeIllegalAccessException (
			@NonNull IllegalAccessException cause) {

		super (
			cause);

	}

}
