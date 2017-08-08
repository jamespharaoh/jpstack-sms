package wbs.utils.time.core;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOf;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public
interface TimeFormatterTimezoneMethods {

	// timezone string

	String timezoneStringLong (
			DateTime dateTime);

	String timezoneStringShort (
			DateTime dateTime);

	// time zone parse

	DateTimeZone timezoneParseRequired (
			String name);

	default
	Optional <DateTimeZone> timezoneParse (
			@NonNull String name) {

		try {

			return optionalOf (
				timezoneParseRequired (
					name));

		} catch (IllegalArgumentException illegalArgumentException) {

			return optionalAbsent ();

		}

	}

}
