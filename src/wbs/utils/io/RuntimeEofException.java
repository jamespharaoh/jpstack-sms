package wbs.utils.io;

import java.io.EOFException;

import lombok.NonNull;

public
class RuntimeEofException
	extends RuntimeIoException {

	public
	RuntimeEofException () {

		super ();

	}

	public
	RuntimeEofException (
			@NonNull EOFException cause) {

		super (
			cause);

	}

}
