package wbs.web.utils;

import static wbs.utils.time.TimeUtils.toInstant;

import java.util.Locale;

import lombok.NonNull;

import org.joda.time.Instant;
import org.joda.time.ReadableInstant;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public
class HttpTimeUtils {

	public static
	String httpTimestampString (
			@NonNull ReadableInstant instant) {

		return httpTimestampFormat.print (
			instant);

	}

	public static
	Instant httpTimestampParseRequired (
			@NonNull String string) {

		return toInstant (
			httpTimestampFormat.parseDateTime (
				string));

	}

	// static data

	public static
	DateTimeFormatter httpTimestampFormat =
		DateTimeFormat.forPattern (
			"EEE, dd MMM yyyyy HH:mm:ss z")

		.withLocale (
			Locale.US)

		.withZoneUTC ()

	;

}
