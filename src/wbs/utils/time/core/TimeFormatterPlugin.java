package wbs.utils.time.core;

import org.joda.time.format.DateTimeFormatter;

public
interface TimeFormatterPlugin {

	String name ();

	DateTimeFormatter timestampSecondFormat ();
	DateTimeFormatter timestampMinuteFormat ();
	DateTimeFormatter timestampHourFormat ();

	DateTimeFormatter timestampTimezoneSecondFormat ();
	DateTimeFormatter timestampTimezoneMinuteFormat ();
	DateTimeFormatter timestampTimezoneHourFormat ();

	DateTimeFormatter timestampTimezoneSecondShortFormat ();
	DateTimeFormatter timestampTimezoneMinuteShortFormat ();
	DateTimeFormatter timestampTimezoneHourShortFormat ();

	DateTimeFormatter timestampTimezoneSecondLongFormat ();
	DateTimeFormatter timestampTimezoneMinuteLongFormat ();
	DateTimeFormatter timestampTimezoneHourLongFormat ();

	DateTimeFormatter longDateFormat ();
	DateTimeFormatter shortDateFormat ();

	DateTimeFormatter timeFormat ();

	DateTimeFormatter timezoneLongFormat ();
	DateTimeFormatter timezoneShortFormat ();

}
