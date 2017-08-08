package wbs.web.exceptions;

import java.util.List;

import lombok.NonNull;

public
class HttpServerErrorException
	extends HttpStatusException {

	public
	HttpServerErrorException (
			@NonNull Long statusCode,
			@NonNull String statusMessage,
			@NonNull List <String> errors) {

		super (
			statusCode,
			statusMessage,
			errors);

	}

}
