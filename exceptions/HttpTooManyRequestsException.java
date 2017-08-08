package wbs.web.exceptions;

import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOr;

import java.util.List;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.web.misc.HttpStatus;

public
class HttpTooManyRequestsException
	extends HttpClientException {

	public
	HttpTooManyRequestsException (
			@NonNull Optional <String> statusMessage,
			@NonNull List <String> errors) {

		super (
			HttpStatus.httpTooManyRequests,
			optionalOr (
				statusMessage,
				"Too many equests"),
			errors);

	}

	public
	HttpTooManyRequestsException () {

		this (
			optionalAbsent (),
			emptyList ());

	}

}
