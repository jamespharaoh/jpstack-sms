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
interface TimeFormatterTimestampTimezoneShortMethods {

	// timestamp timezone short string

	String timestampTimezoneSecondShortString (
			ReadableDateTime dateTime);

	default
	String timestampTimezoneSecondShortString (
			DateTimeZone timeZone,
			ReadableInstant instant) {

		return timestampTimezoneSecondShortString (
			instant.toInstant ().toDateTime (
				timeZone));

	}

	String timestampTimezoneMinuteShortString (
			ReadableDateTime dateTime);

	default
	String timestampTimezoneMinuteShortString (
			DateTimeZone timeZone,
			ReadableInstant instant) {

		return timestampTimezoneMinuteShortString (
			instant.toInstant ().toDateTime (
				timeZone));

	}

	String timestampTimezoneHourShortString (
			ReadableDateTime dateTime);

	default
	String timestampTimezoneHourShortString (
			DateTimeZone timeZone,
			ReadableInstant instant) {

		return timestampTimezoneHourShortString (
			instant.toInstant ().toDateTime (
				timeZone));

	}

	// timestamp timezone short parse

	DateTime timestampTimezoneSecondShortParseRequired (
			String string);

	default
	Optional <DateTime> timestampTimezoneSecondShortParse (
			@NonNull String string) {

		try {

			return optionalOf (
				timestampTimezoneSecondShortParseRequired (
					string));

		} catch (IllegalArgumentException illegalArgumentException) {

			return optionalAbsent ();

		}
	}

	DateTime timestampTimezoneMinuteShortParseRequired (
			String string);

	default
	Optional <DateTime> timestampTimezoneMinuteShortParse (
			@NonNull String string) {

		try {

			return optionalOf (
				timestampTimezoneMinuteShortParseRequired (
					string));

		} catch (IllegalArgumentException illegalArgumentException) {

			return optionalAbsent ();

		}

	}

	DateTime timestampTimezoneHourShortParseRequired (
			String string);

	default
	Optional <DateTime> timestampTimezoneHourShortParse (
			@NonNull String string) {

		try {

			return optionalOf (
				timestampTimezoneHourShortParseRequired (
					string));

		} catch (IllegalArgumentException illegalArgumentException) {

			return optionalAbsent ();

		}

	}

}
