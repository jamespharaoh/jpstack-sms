package wbs.utils.exception;

import java.nio.charset.MalformedInputException;

import lombok.NonNull;

public
class RuntimeMalformedInputException
	extends RuntimeCharacterCodingException {

	public
	RuntimeMalformedInputException (
			@NonNull MalformedInputException cause) {

		super (
			cause);

	}

}
