package wbs.utils.time.core;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOf;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.ReadableDateTime;
import org.joda.time.ReadableInstant;

public
interface TimeFormatterTimestampMethods {

	// timestamp string

	String timestampSecondString (
			ReadableDateTime dateTime);

	default
	String timestampSecondString (
			@NonNull DateTimeZone timeZone,
			@NonNull ReadableInstant instant) {

		return timestampSecondString (
			instant.toInstant ().toDateTime (
				timeZone));

	}

	String timestampMinuteString (
			ReadableDateTime dateTime);

	default
	String timestampMinuteString (
			@NonNull DateTimeZone timeZone,
			@NonNull ReadableInstant instant) {

		return timestampMinuteString (
			instant.toInstant ().toDateTime (
				timeZone));

	}

	String timestampHourString (
			ReadableDateTime dateTime);

	default
	String timestampHourString (
			@NonNull DateTimeZone timeZone,
			@NonNull ReadableInstant instant) {

		return timestampHourString (
			instant.toInstant ().toDateTime (
				timeZone));

	}

	// timestamp parse

	DateTime timestampSecondParseRequired (
			DateTimeZone timeZone,
			String string);

	default
	Optional <DateTime> timestampSecondParse (
			@NonNull DateTimeZone timeZone,
			@NonNull String string) {

		try {

			return optionalOf (
				timestampSecondParseRequired (
					timeZone,
					string));

		} catch (IllegalArgumentException illegalArgumentException) {

			return optionalAbsent ();

		}

	}

	DateTime timestampMinuteParseRequired (
			DateTimeZone timeZone,
			String string);

	default
	Optional <DateTime> timestampMinuteParse (
			@NonNull DateTimeZone timeZone,
			@NonNull String string) {

		try {

			return optionalOf (
				timestampMinuteParseRequired (
					timeZone,
					string));

		} catch (IllegalArgumentException illegalArgumentException) {

			return optionalAbsent ();

		}

	}

	DateTime timestampHourParseRequired (
			DateTimeZone timeZone,
			String string);

	default
	Optional <DateTime> timestampHourParse (
			@NonNull DateTimeZone timeZone,
			@NonNull String string) {

		try {

			return optionalOf (
				timestampHourParseRequired (
					timeZone,
					string));

		} catch (IllegalArgumentException illegalArgumentException) {

			return optionalAbsent ();

		}

	}

}
