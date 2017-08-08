package wbs.utils.exception;

import java.lang.reflect.InvocationTargetException;

import lombok.NonNull;

public
class RuntimeInvocationTargetException
	extends RuntimeReflectiveOperationException {

	public
	RuntimeInvocationTargetException (
			@NonNull InvocationTargetException cause) {

		super (
			cause);

	}

}
