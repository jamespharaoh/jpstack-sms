package wbs.utils.exception;

import java.io.UnsupportedEncodingException;

import lombok.NonNull;

public
class RuntimeUnsupportedEncodingException
	extends RuntimeException {

	public
	RuntimeUnsupportedEncodingException (
			@NonNull UnsupportedEncodingException cause) {

		super (
			cause);

	}

}
