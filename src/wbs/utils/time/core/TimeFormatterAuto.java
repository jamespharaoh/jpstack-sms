package wbs.utils.time.core;

import lombok.NonNull;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.ReadableDateTime;

public
interface TimeFormatterAuto
	extends TimeFormatterMethods {

	// plugin

	TimeFormatterPlugin plugin ();

	@Override
	default
	String name () {
		return plugin ().name ();
	}

	// timestamp string

	@Override
	default
	String timestampSecondString (
			@NonNull ReadableDateTime dateTime) {

		return plugin ().timestampSecondFormat ().print (
			dateTime);

	}

	@Override
	default
	String timestampMinuteString (
			@NonNull ReadableDateTime dateTime) {

		return plugin ().timestampMinuteFormat ().print (
			dateTime);

	}

	@Override
	default
	String timestampHourString (
			@NonNull ReadableDateTime dateTime) {

		return plugin ().timestampHourFormat ().print (
			dateTime);

	}

	// timestamp timezone string

	@Override
	default
	String timestampTimezoneSecondString (
			@NonNull ReadableDateTime dateTime) {

		return plugin ().timestampTimezoneSecondFormat ().print (
			dateTime);

	}

	@Override
	default
	String timestampTimezoneMinuteString (
			@NonNull ReadableDateTime dateTime) {

		return plugin ().timestampTimezoneMinuteFormat ().print (
			dateTime);

	}

	@Override
	default
	String timestampTimezoneHourString (
			@NonNull ReadableDateTime dateTime) {

		return plugin ().timestampTimezoneHourFormat ().print (
			dateTime);

	}

	// timestamp timezone short string

	@Override
	default
	String timestampTimezoneSecondShortString (
			@NonNull ReadableDateTime dateTime) {

		return plugin ().timestampTimezoneSecondShortFormat ().print (
			dateTime);

	}

	@Override
	default
	String timestampTimezoneMinuteShortString (
			@NonNull ReadableDateTime dateTime) {

		return plugin ().timestampTimezoneMinuteShortFormat ().print (
			dateTime);

	}

	@Override
	default
	String timestampTimezoneHourShortString (
			@NonNull ReadableDateTime dateTime) {

		return plugin ().timestampTimezoneHourShortFormat ().print (
			dateTime);

	}

	// timestamp timezone long string

	@Override
	default
	String timestampTimezoneSecondLongString (
			@NonNull ReadableDateTime dateTime) {

		return plugin ().timestampTimezoneSecondLongFormat ().print (
			dateTime);

	}

	@Override
	default
	String timestampTimezoneMinuteLongString (
			@NonNull ReadableDateTime dateTime) {

		return plugin ().timestampTimezoneMinuteLongFormat ().print (
			dateTime);

	}

	@Override
	default
	String timestampTimezoneHourLongString (
			@NonNull ReadableDateTime dateTime) {

		return plugin ().timestampTimezoneHourLongFormat ().print (
			dateTime);

	}

	// date string

	@Override
	default
	String dateStringLong (
			@NonNull LocalDate localDate) {

		return plugin ().longDateFormat ().print (
			localDate);

	}

	@Override
	default
	String dateStringShort (
			@NonNull LocalDate localDate) {

		return plugin ().shortDateFormat ().print (
			localDate);

	}

	// time string

	@Override
	default
	String timeString (
			@NonNull LocalTime time) {

		return plugin ().timeFormat ().print (
			time);

	}

	// timezone string

	@Override
	default
	String timezoneStringLong (
			@NonNull DateTime dateTime) {

		return plugin ().timezoneLongFormat ().print (
			dateTime);

	}

	@Override
	default
	String timezoneStringShort (
			@NonNull DateTime dateTime) {

		return plugin ().timezoneShortFormat ().print (
			dateTime);

	}

	// timestamp parse

	@Override
	default
	DateTime timestampSecondParseRequired (
			@NonNull DateTimeZone timeZone,
			@NonNull String string) {

		return plugin ().timestampSecondFormat ()
			.withZone (timeZone)
			.parseDateTime (string);

	}

	@Override
	default
	DateTime timestampMinuteParseRequired (
			@NonNull DateTimeZone timeZone,
			@NonNull String string) {

		return plugin ().timestampMinuteFormat ()
			.withZone (timeZone)
			.parseDateTime (string);

	}

	@Override
	default
	DateTime timestampHourParseRequired (
			@NonNull DateTimeZone timeZone,
			@NonNull String string) {

		return plugin ().timestampHourFormat ()
			.withZone (timeZone)
			.parseDateTime (string);

	}

	// timestamp timezone parse

	@Override
	default
	DateTime timestampTimezoneSecondParseRequired (
			@NonNull String string) {

		return plugin ().timestampTimezoneSecondFormat ()
			.parseDateTime (string);

	}

	@Override
	default
	DateTime timestampTimezoneMinuteParseRequired (
			@NonNull String string) {

		return plugin ().timestampTimezoneMinuteFormat ()
			.parseDateTime (string);

	}

	@Override
	default
	DateTime timestampTimezoneHourParseRequired (
			@NonNull String string) {

		return plugin ().timestampTimezoneHourFormat ()
			.parseDateTime (string);

	}

	// timestamp timezone short parse

	@Override
	default
	DateTime timestampTimezoneSecondShortParseRequired (
			@NonNull String string) {

		return plugin ().timestampTimezoneSecondShortFormat ()
			.parseDateTime (string);

	}

	@Override
	default
	DateTime timestampTimezoneMinuteShortParseRequired (
			@NonNull String string) {

		return plugin ().timestampTimezoneMinuteShortFormat ()
			.parseDateTime (string);

	}

	@Override
	default
	DateTime timestampTimezoneHourShortParseRequired (
			@NonNull String string) {

		return plugin ().timestampTimezoneHourShortFormat ()
			.parseDateTime (string);

	}

	// timestamp timezone long parse

	@Override
	default
	DateTime timestampTimezoneSecondLongParseRequired (
			@NonNull String string) {

		return plugin ().timestampTimezoneSecondLongFormat ()
			.parseDateTime (string);

	}

	@Override
	default
	DateTime timestampTimezoneMinuteLongParseRequired (
			@NonNull String string) {

		return plugin ().timestampTimezoneMinuteLongFormat ()
			.parseDateTime (string);

	}

	@Override
	default
	DateTime timestampTimezoneHourLongParseRequired (
			@NonNull String string) {

		return plugin ().timestampTimezoneHourLongFormat ()
			.parseDateTime (string);

	}

	// date parse

	@Override
	default
	LocalDate dateParseRequired (
			@NonNull String string) {

		return plugin ().shortDateFormat ().parseLocalDate (
			string);

	}

	// timezone parse

	@Override
	default
	DateTimeZone timezoneParseRequired (
			@NonNull String name) {

		return DateTimeZone.forID (
			name);

	}

}
