package wbs.utils.time.core;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOf;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.ReadableInstant;

public
interface TimeFormatterDateMethods {

	// date string

	String dateStringLong (
			LocalDate localDate);

	default
	String dateStringLong (
			@NonNull DateTime dateTime) {

		return dateStringLong (
			dateTime.toLocalDate ());

	}

	default
	String dateStringLong (
			@NonNull DateTimeZone timeZone,
			@NonNull ReadableInstant instant) {

		return dateStringLong (
			instant.toInstant ()
				.toDateTime (timeZone)
				.toLocalDate ());

	}

	String dateStringShort (
			LocalDate localDate);

	default
	String dateStringShort (
			@NonNull DateTime dateTime) {

		return dateStringShort (
			dateTime.toLocalDate ());

	}

	default
	String dateStringShort (
			@NonNull DateTimeZone timeZone,
			@NonNull ReadableInstant instant) {

		return dateStringShort (
			instant.toInstant ()
				.toDateTime (timeZone)
				.toLocalDate ());

	}

	// date parse

	LocalDate dateParseRequired (
			String string);

	default
	Optional <LocalDate> dateParse (
			@NonNull String string) {

		try {

			return optionalOf (
				dateParseRequired (
					string));

		} catch (IllegalArgumentException illegalArgumentException) {

			return optionalAbsent ();

		}

	}

}
