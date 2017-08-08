package wbs.utils.time.core;

import static wbs.utils.time.TimeUtils.localTime;

import lombok.NonNull;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;
import org.joda.time.ReadableDateTime;
import org.joda.time.ReadableInstant;

public
interface TimeFormatterTimeMethods {

	// time string

	String timeString (
			LocalTime time);

	default
	String timeString (
			@NonNull ReadableDateTime dateTime) {

		return timeString (
			localTime (
				dateTime));

	}

	default
	String timeString (
			@NonNull DateTimeZone timeZone,
			@NonNull ReadableInstant instant) {

		return timeString (
			localTime (
				timeZone,
				instant));

	}

}
