package wbs.web.exceptions;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;

import com.google.common.collect.ImmutableList;

import lombok.NonNull;

public
class HttpStatusException
	extends RuntimeException {

	private final
	Long statusCode;

	private final
	String statusMessage;

	private
	List <String> errors =
		ImmutableList.of ();

	public
	HttpStatusException (
			@NonNull Long statusCode,
			@NonNull String statusMessage,
			@NonNull List <String> errors) {

		super (
			stringFormat (
				"%s: %s",
				integerToDecimalString (
					statusCode),
				statusMessage));

		this.statusCode =
			statusCode;

		this.statusMessage =
			statusMessage;

		this.errors =
			errors;

	}

	public
	Long statusCode () {
		return statusCode;
	}

	public
	String statusMessage () {
		return statusMessage;
	}

	public
	List <String> errors () {
		return errors;
	}

}
