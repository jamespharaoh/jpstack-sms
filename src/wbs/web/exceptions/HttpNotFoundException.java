package wbs.web.exceptions;

import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOr;

import java.util.List;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.web.misc.HttpStatus;

public
class HttpNotFoundException
	extends HttpClientException {

	public
	HttpNotFoundException (
			@NonNull Optional <String> statusMessage,
			@NonNull List <String> errors) {

		super (
			HttpStatus.httpNotFound,
			optionalOr (
				statusMessage,
				"Not found"),
			errors);

	}

	public
	HttpNotFoundException () {

		this (
			optionalAbsent (),
			emptyList ());

	}

}
