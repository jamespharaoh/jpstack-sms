package wbs.utils.io;

import java.io.InterruptedIOException;

import lombok.NonNull;

public
class RuntimeInterruptedIoException
	extends RuntimeIoException {

	public
	RuntimeInterruptedIoException () {

		super ();

	}

	public
	RuntimeInterruptedIoException (
			@NonNull InterruptedIOException cause) {

		super (
			cause);

	}

}
