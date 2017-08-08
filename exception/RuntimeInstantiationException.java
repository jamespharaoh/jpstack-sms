package wbs.utils.exception;

import lombok.NonNull;

public
class RuntimeInstantiationException
	extends RuntimeReflectiveOperationException {

	public
	RuntimeInstantiationException (
			@NonNull InstantiationException cause) {

		super (
			cause);

	}

}
