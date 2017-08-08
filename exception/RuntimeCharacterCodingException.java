package wbs.utils.exception;

import java.nio.charset.CharacterCodingException;

import lombok.NonNull;

import wbs.utils.io.RuntimeIoException;

public
class RuntimeCharacterCodingException
	extends RuntimeIoException {

	public
	RuntimeCharacterCodingException (
			@NonNull CharacterCodingException cause) {

		super (
			cause);

	}

}
