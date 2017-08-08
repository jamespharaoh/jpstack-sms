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
interface TimeFormatterTimestampTimezoneLongMethods {

	// timestamp timezone Long string

	String timestampTimezoneSecondLongString (
			ReadableDateTime dateTime);

	default
	String timestampTimezoneSecondLongString (
			DateTimeZone timeZone,
			ReadableInstant instant) {

		return timestampTimezoneSecondLongString (
			instant.toInstant ().toDateTime (
				timeZone));

	}

	String timestampTimezoneMinuteLongString (
			ReadableDateTime dateTime);

	default
	String timestampTimezoneMinuteLongString (
			DateTimeZone timeZone,
			ReadableInstant instant) {

		return timestampTimezoneMinuteLongString (
			instant.toInstant ().toDateTime (
				timeZone));

	}

	String timestampTimezoneHourLongString (
			ReadableDateTime dateTime);

	default
	String timestampTimezoneHourLongString (
			DateTimeZone timeZone,
			ReadableInstant instant) {

		return timestampTimezoneHourLongString (
			instant.toInstant ().toDateTime (
				timeZone));

	}

	// timestamp timezone Long parse

	DateTime timestampTimezoneSecondLongParseRequired (
			String string);

	default
	Optional <DateTime> timestampTimezoneSecondLongParse (
			@NonNull String string) {

		try {

			return optionalOf (
				timestampTimezoneSecondLongParseRequired (
					string));

		} catch (IllegalArgumentException illegalArgumentException) {

			return optionalAbsent ();

		}
	}

	DateTime timestampTimezoneMinuteLongParseRequired (
			String string);

	default
	Optional <DateTime> timestampTimezoneMinuteLongParse (
			@NonNull String string) {

		try {

			return optionalOf (
				timestampTimezoneMinuteLongParseRequired (
					string));

		} catch (IllegalArgumentException illegalArgumentException) {

			return optionalAbsent ();

		}

	}

	DateTime timestampTimezoneHourLongParseRequired (
			String string);

	default
	Optional <DateTime> timestampTimezoneHourLongParse (
			@NonNull String string) {

		try {

			return optionalOf (
				timestampTimezoneHourLongParseRequired (
					string));

		} catch (IllegalArgumentException illegalArgumentException) {

			return optionalAbsent ();

		}

	}

}
