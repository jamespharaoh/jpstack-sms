package wbs.utils.time.core;

import lombok.Data;
import lombok.experimental.Accessors;

import org.joda.time.format.DateTimeFormatter;

@Accessors (fluent = true)
@Data
public
class TimeFormatterPluginImplementation
	implements TimeFormatterPlugin {

	String name;

	DateTimeFormatter timestampSecondFormat;
	DateTimeFormatter timestampMinuteFormat;
	DateTimeFormatter timestampHourFormat;

	DateTimeFormatter timestampTimezoneSecondFormat;
	DateTimeFormatter timestampTimezoneMinuteFormat;
	DateTimeFormatter timestampTimezoneHourFormat;

	DateTimeFormatter timestampTimezoneSecondShortFormat;
	DateTimeFormatter timestampTimezoneMinuteShortFormat;
	DateTimeFormatter timestampTimezoneHourShortFormat;

	DateTimeFormatter timestampTimezoneSecondLongFormat;
	DateTimeFormatter timestampTimezoneMinuteLongFormat;
	DateTimeFormatter timestampTimezoneHourLongFormat;

	DateTimeFormatter longDateFormat;
	DateTimeFormatter shortDateFormat;

	DateTimeFormatter timeFormat;

	DateTimeFormatter timezoneLongFormat;
	DateTimeFormatter timezoneShortFormat;

}
