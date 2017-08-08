package wbs.web.exceptions;

import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.collection.MapUtils.mapItemForKey;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.TypeUtils.classInstantiate;

import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import wbs.web.misc.HttpStatus;

public
class HttpClientException
	extends HttpStatusException {

	public
	HttpClientException (
			@NonNull Long statusCode,
			@NonNull String statusMessage,
			@NonNull List <String> errors) {

		super (
			statusCode,
			statusMessage,
			errors);

	}

	public static
	Map <Long, Class <? extends HttpClientException>> exceptionClassesByStatus =
		ImmutableMap.<Long, Class <? extends HttpClientException>> builder ()

		.put (
			HttpStatus.httpBadRequest,
			HttpBadRequestException.class)

		.put (
			HttpStatus.httpMethodNotAllowed,
			HttpMethodNotAllowedException.class)

		.put (
			HttpStatus.httpNotFound,
			HttpNotFoundException.class)

		.put (
			HttpStatus.httpTooManyRequests,
			HttpTooManyRequestsException.class)

		.put (
			HttpStatus.httpUnprocessableEntity,
			HttpUnprocessableEntityException.class)

		.build ()

	;

	public static
	HttpClientException forStatus (
			@NonNull Long statusCode,
			@NonNull String statusMessage) {

		Optional <Class <? extends HttpClientException>>
			exceptionClassOptional =
				mapItemForKey (
					exceptionClassesByStatus,
					statusCode);

		if (
			optionalIsPresent (
				exceptionClassOptional)
		) {

			Class <? extends HttpClientException> exceptionClass =
				optionalGetRequired (
					exceptionClassOptional);

			return classInstantiate (
				exceptionClass,
				ImmutableList.<Class <?>> of (
					Optional.class,
					List.class),
				ImmutableList.<Object> of (
					optionalOf (
						statusMessage),
					emptyList ()));

		} else {

			return new HttpClientException (
				statusCode,
				statusMessage,
				emptyList ());

		}

	}

}
