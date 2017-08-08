package wbs.utils.io;

import java.io.IOException;

import lombok.NonNull;

public
class RuntimeIoException
	extends RuntimeException {

	public
	RuntimeIoException () {

		super ();

	}

	public
	RuntimeIoException (
			@NonNull IOException cause) {

		super (
			cause);

	}

}
