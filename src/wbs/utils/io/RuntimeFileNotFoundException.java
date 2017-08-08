package wbs.utils.io;

import java.io.FileNotFoundException;

import lombok.NonNull;

public
class RuntimeFileNotFoundException
	extends RuntimeIoException {

	public
	RuntimeFileNotFoundException (
			@NonNull FileNotFoundException cause) {

		super (
			cause);

	}

}
