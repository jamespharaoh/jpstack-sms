package wbs.utils.time.core;

import static wbs.utils.collection.CollectionUtils.iterableFirstElement;
import static wbs.utils.etc.OptionalUtils.optionalOrThrow;
import static wbs.utils.etc.OptionalUtils.presentInstances;
import static wbs.utils.string.StringUtils.stringFormat;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public
interface TimeFormatterMethods
	extends
		TimeFormatterCoreMethods,
		TimeFormatterDateMethods,
		TimeFormatterTimeMethods,
		TimeFormatterTimestampMethods,
		TimeFormatterTimestampTimezoneMethods,
		TimeFormatterTimestampTimezoneLongMethods,
		TimeFormatterTimestampTimezoneShortMethods,
		TimeFormatterTimezoneMethods {

	// timestamp auto parse

	default
	Optional <DateTime> timestampParseAuto (
			@NonNull DateTimeZone timezone,
			@NonNull String string) {

		return iterableFirstElement (
			presentInstances (

			() -> timestampTimezoneSecondParse (
				string),

			() -> timestampTimezoneMinuteParse (
				string),

			() -> timestampTimezoneHourParse (
				string),

			() -> timestampSecondParse (
				timezone,
				string),

			() -> timestampMinuteParse (
				timezone,
				string),

			() -> timestampHourParse (
				timezone,
				string)

		));

	}

	default
	DateTime timestampParseAutoRequired (
			@NonNull DateTimeZone timezone,
			@NonNull String string) {

		return optionalOrThrow (
			timestampParseAuto (
				timezone,
				string),
			() -> new IllegalArgumentException (
				stringFormat (
					"Unable to parse timestamp: %s",
					string)));

	}

}
