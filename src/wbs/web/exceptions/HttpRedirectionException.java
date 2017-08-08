package wbs.web.exceptions;

import java.util.List;

import lombok.NonNull;

public
class HttpRedirectionException
	extends HttpStatusException {

	public
	HttpRedirectionException (
			@NonNull Long statusCode,
			@NonNull String statusMessage,
			@NonNull List <String> errors) {

		super (
			statusCode,
			statusMessage,
			errors);

	}

}
