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
interface TimeFormatterTimestampTimezoneMethods {

	// timestamp timezone string

	String timestampTimezoneSecondString (
			ReadableDateTime dateTime);

	default
	String timestampTimezoneSecondString (
			DateTimeZone timeZone,
			ReadableInstant instant) {

		return timestampTimezoneSecondString (
			instant.toInstant ().toDateTime (
				timeZone));

	}

	String timestampTimezoneMinuteString (
			ReadableDateTime dateTime);

	default
	String timestampTimezoneMinuteString (
			DateTimeZone timeZone,
			ReadableInstant instant) {

		return timestampTimezoneMinuteString (
			instant.toInstant ().toDateTime (
				timeZone));

	}

	String timestampTimezoneHourString (
			ReadableDateTime dateTime);

	default
	String timestampTimezoneHourString (
			DateTimeZone timeZone,
			ReadableInstant instant) {

		return timestampTimezoneHourString (
			instant.toInstant ().toDateTime (
				timeZone));

	}

	// timestamp timezone parse

	DateTime timestampTimezoneSecondParseRequired (
			String string);

	default
	Optional <DateTime> timestampTimezoneSecondParse (
			@NonNull String string) {

		try {

			return optionalOf (
				timestampTimezoneSecondParseRequired (
					string));

		} catch (IllegalArgumentException illegalArgumentException) {

			return optionalAbsent ();

		}
	}

	DateTime timestampTimezoneMinuteParseRequired (
			String string);

	default
	Optional <DateTime> timestampTimezoneMinuteParse (
			@NonNull String string) {

		try {

			return optionalOf (
				timestampTimezoneMinuteParseRequired (
					string));

		} catch (IllegalArgumentException illegalArgumentException) {

			return optionalAbsent ();

		}

	}

	DateTime timestampTimezoneHourParseRequired (
			String string);

	default
	Optional <DateTime> timestampTimezoneHourParse (
			@NonNull String string) {

		try {

			return optionalOf (
				timestampTimezoneHourParseRequired (
					string));

		} catch (IllegalArgumentException illegalArgumentException) {

			return optionalAbsent ();

		}

	}

}
