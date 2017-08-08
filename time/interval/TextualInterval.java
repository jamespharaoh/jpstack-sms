package wbs.utils.time.interval;

import static wbs.utils.collection.CollectionUtils.collectionDoesNotHaveThreeElements;
import static wbs.utils.collection.CollectionUtils.collectionDoesNotHaveTwoElements;
import static wbs.utils.collection.CollectionUtils.listFirstElementRequired;
import static wbs.utils.collection.CollectionUtils.listSecondElementRequired;
import static wbs.utils.collection.CollectionUtils.listThirdElementRequired;
import static wbs.utils.etc.Misc.stringTrim;
import static wbs.utils.etc.NumberUtils.integerNotEqualSafe;
import static wbs.utils.etc.NumberUtils.lessThanOne;
import static wbs.utils.etc.NumberUtils.parseInteger;
import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.lowercase;
import static wbs.utils.string.StringUtils.stringContains;
import static wbs.utils.string.StringUtils.stringEndsWithSimple;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringInSafe;
import static wbs.utils.string.StringUtils.stringNotEqualSafe;
import static wbs.utils.string.StringUtils.stringNotInSafe;
import static wbs.utils.string.StringUtils.stringSplitSimple;
import static wbs.utils.string.StringUtils.stringSplitSpace;
import static wbs.utils.string.StringUtils.stringStartsWithSimple;
import static wbs.utils.time.TimeUtils.millisToInstant;

import java.io.Serializable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.tuple.Pair;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.MutableInterval;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.ReadableInstant;
import org.joda.time.ReadableInterval;
import org.joda.time.YearMonth;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

@Accessors (fluent = true)
@Value
public
class TextualInterval
	implements
		ReadableInterval,
		Serializable {

	String sourceText;
	String genericText;

	Interval value;

	public static
	boolean validPartial (
			@NonNull String string) {

		if (
			stringInSafe (
				lowercase (
					string),
				"today",
				"yesterday",
				"this month",
				"last month")
		) {
			return true;
		}

		for (
			Pattern pattern
				: partialPatterns
		) {

			Matcher matcher =
				pattern.matcher (
					string);

			if (matcher.matches ())
				return true;

		}

		return false;

	}

	public static
	boolean valid (
			@NonNull String originalString) {

		String string =
			stringTrim (
				originalString);

		if (
			stringContains (
				" to ",
				string)
		) {

			List <String> parts =
				stringSplitSimple (
					" to ",
					string);

			if (
				collectionDoesNotHaveTwoElements (
					parts)
			) {
				return false;
			}

			return (

				validPartial (
					stringTrim (
						listFirstElementRequired (
							parts)))

				&& validPartial (
					stringTrim (
						listSecondElementRequired (
							parts)))

			);

		} else if (
			stringStartsWithSimple (
				"last ",
				string)
		) {

			if (

				stringEndsWithSimple (
					" years",
					string)

				|| stringEndsWithSimple (
					" months",
					string)

				|| stringEndsWithSimple (
					" days",
					string)

				|| stringEndsWithSimple (
					" hours",
					string)

				|| stringEndsWithSimple (
					" minutes",
					string)

				|| stringEndsWithSimple (
					" seconds",
					string)

			) {

				List <String> parts =
					stringSplitSpace (
						string);

				if (
					collectionDoesNotHaveThreeElements (
						parts)
				) {
					return false;
				}

				String numericPart =
					listSecondElementRequired (
						parts);

				Optional <Long> numberOptional =
					parseInteger (
						numericPart);

				if (
					optionalIsNotPresent (
						numberOptional)
				) {
					return false;
				}

				Long number =
					optionalGetRequired (
						numberOptional);

				if (
					lessThanOne (
						number)
				) {
					return false;
				}

				return true;

			} else {

				return false;

			}

		} else {

			return validPartial (
				string);

		}

	}

	public static
	Optional <Pair <Interval, String>> parsePartialSymbolic (
			@NonNull DateTimeZone timezone,
			@NonNull String string,
			@NonNull Long hourOffset) {

		DateTime now =
			DateTime.now (
				timezone);

		LocalDate today =
			now.getHourOfDay () >= hourOffset
				? now.toLocalDate ()
				: now.toLocalDate ().minusDays (1);

		YearMonth thisMonth =
			now.getHourOfDay () >= hourOffset || now.getDayOfMonth () > 1
				? new YearMonth (now)
				: new YearMonth (now).minusMonths (1);

		switch (lowercase (string)) {

		case "today":

			LocalDate tomorrow =
				today.plusDays (1);

			return Optional.of (
				Pair.of (
					new Interval (
						today.toDateTime (
							new LocalTime (
								toJavaIntegerRequired (
									hourOffset),
								0),
							timezone),
						tomorrow.toDateTime (
							new LocalTime (
								toJavaIntegerRequired (
									hourOffset),
								0),
							timezone)),
					today.toString (
						dateFormat)));

		case "yesterday":

			LocalDate yesterday =
				today.minusDays (1);

			return Optional.of (
				Pair.of (
					new Interval (
						yesterday.toDateTime (
							new LocalTime (
								toJavaIntegerRequired (
									hourOffset),
								0),
							timezone),
						today.toDateTime (
							new LocalTime (
								toJavaIntegerRequired (
									hourOffset),
								0),
							timezone)),
					yesterday.toString (
						dateFormat)));

		case "this month":

			YearMonth nextMonth =
				thisMonth.plusMonths (1);

			return Optional.of (
				Pair.of (
					new Interval (
						thisMonth.toLocalDate (1).toDateTime (
							new LocalTime (
								toJavaIntegerRequired (
									hourOffset),
								0),
							timezone),
						nextMonth.toLocalDate (1).toDateTime (
							new LocalTime (
								toJavaIntegerRequired (
									hourOffset),
								0),
							timezone)),
					thisMonth.toString (
						monthFormat)));

		case "last month":

			YearMonth lastMonth =
				thisMonth.minusMonths (1);

			return Optional.of (
				Pair.of (
					new Interval (
						lastMonth.toLocalDate (1).toDateTime (
							new LocalTime (
								toJavaIntegerRequired (
									hourOffset),
								0),
							timezone),
						thisMonth.toLocalDate (1).toDateTime (
							new LocalTime (
								toJavaIntegerRequired (
									hourOffset),
								0),
							timezone)),
					lastMonth.toString (
						monthFormat)));

		}

		return Optional.absent ();

	}

	public static
	Optional <TextualInterval> parseRecent (
			@NonNull DateTimeZone timezone,
			@NonNull String string) {

		List <String> parts =
			stringSplitSpace (
				stringTrim (
					string));

		if (
			collectionDoesNotHaveThreeElements (
				parts)
		) {
			return optionalAbsent ();
		}

		if (
			stringNotEqualSafe (
				"last",
				listFirstElementRequired (
					parts))
		) {
			return optionalAbsent ();
		}

		String numericPart =
			listSecondElementRequired (
				parts);

		Optional <Long> numberOptional =
			parseInteger (
				numericPart);

		if (
			optionalIsNotPresent (
				numberOptional)
		) {
			return optionalAbsent ();
		}

		Long number =
			optionalGetRequired (
				numberOptional);

		if (
			lessThanOne (
				number)
		) {
			return optionalAbsent ();
		}

		String unitPart =
			listThirdElementRequired (
				parts);

		DateTime now =
			DateTime.now (
				timezone);

		DateTime start;

		if (
			stringInSafe (
				unitPart,
				"year",
				"years")
		) {

			start =
				now.toDateTime ().minusYears (
					toJavaIntegerRequired (
						number));

		} else if (
			stringInSafe (
				unitPart,
				"month",
				"months")
		) {

			start =
				now.toDateTime ().minusMonths (
					toJavaIntegerRequired (
						number));

		} else if (
			stringInSafe (
				unitPart,
				"day",
				"days")
		) {

			start =
				now.toDateTime ().minusDays (
					toJavaIntegerRequired (
						number));

		} else if (
			stringInSafe (
				unitPart,
				"hour",
				"hours")
		) {

			start =
				now.toDateTime ().minusHours (
					toJavaIntegerRequired (
						number));

		} else if (
			stringInSafe (
				unitPart,
				"minute",
				"minutes")
		) {

			start =
				now.toDateTime ().minusMinutes (
					toJavaIntegerRequired (
						number));

		} else if (
			stringInSafe (
				unitPart,
				"second",
				"seconds")
		) {

			start =
				now.toDateTime ().minusSeconds (
					toJavaIntegerRequired (
						number));

		} else {

			return optionalAbsent ();

		}

		return optionalOf (
			new TextualInterval (
				string,
				stringFormat (
					"%s to %s",
					start.toString (
						timestampFormat),
					now.toString (
						timestampFormat)),
				new Interval (
					start,
					now)));

	}

	public static
	Optional <Pair <Interval, String>> parsePartialNumeric (
			@NonNull DateTimeZone timeZone,
			@NonNull String string,
			@NonNull Long hourOffset) {

		long fromYear = 0;
		long fromMonth = 1;
		long fromDate = 1;
		long fromHour = hourOffset;
		long fromMinute = 0;
		long fromSecond = 0;

		for (
			Pattern pattern
				: partialPatterns
		) {

			Matcher matcher =
				pattern.matcher (
					string);

			if (! matcher.matches ())
				continue;

			int groupCount =
				matcher.groupCount ();

			// work out time from

			if (groupCount >= 1) {

				fromYear =
					Integer.parseInt (
						matcher.group (1));

			}

			if (groupCount >= 2) {

				fromMonth =
					Integer.parseInt (
						matcher.group (2));

			}

			if (groupCount >= 3) {

				fromDate =
					Integer.parseInt (
						matcher.group (3));

			}

			if (groupCount >= 4) {

				fromHour =
					Integer.parseInt (
						matcher.group (4));

			}

			if (groupCount >= 5) {

				fromMinute =
					Integer.parseInt (
						matcher.group (5));

			}

			if (groupCount >= 6) {

				fromSecond =
					Integer.parseInt (
						matcher.group (6));

			}

			DateTime fromDateTime =
				new DateTime (
					toJavaIntegerRequired (
						fromYear),
					toJavaIntegerRequired (
						fromMonth),
					toJavaIntegerRequired (
						fromDate),
					toJavaIntegerRequired (
						fromHour),
					toJavaIntegerRequired (
						fromMinute),
					toJavaIntegerRequired (
						fromSecond),
					timeZone);

			// work out time to

			DateTime toDateTime;

			if (groupCount == 0) {

				toDateTime =
					fromDateTime.plusYears (
						10000);

			} else if (groupCount == 1) {

				toDateTime =
					fromDateTime.plusYears (
						1);

			} else if (groupCount == 2) {

				toDateTime =
					fromDateTime.plusMonths (
						1);

			} else if (groupCount == 3) {

				toDateTime =
					fromDateTime.plusDays (
						1);

			} else if (groupCount == 4) {

				toDateTime =
					fromDateTime.plusHours (
						1);

			} else if (groupCount == 5) {

				toDateTime =
					fromDateTime.plusMinutes (
						1);

			} else if (groupCount == 6) {

				toDateTime =
					fromDateTime.plusSeconds (
						1);

			} else {

				return optionalAbsent ();

			}

			return optionalOf (
				Pair.of (
					new Interval (
						fromDateTime,
						toDateTime),
					string));

		}

		return optionalAbsent ();

	}

	public static
	Optional <Pair <Interval, String>> parsePartial (
			@NonNull DateTimeZone timeZone,
			@NonNull String string,
			@NonNull Long hourOffset) {

		Optional <Pair <Interval, String>> symbolicResult =
			parsePartialSymbolic (
				timeZone,
				string,
				hourOffset);

		if (
			optionalIsPresent (
				symbolicResult)
		) {
			return symbolicResult;
		}

		Optional <Pair <Interval, String>> numericResult =
			parsePartialNumeric (
				timeZone,
				string,
				hourOffset);

		if (
			optionalIsPresent (
				numericResult)
		) {
			return numericResult;
		}

		return optionalAbsent ();

	}

	public static
	Optional <TextualInterval> parse (
			@NonNull DateTimeZone timezone,
			@NonNull String orignalSource,
			@NonNull Long hourOffset) {

		String source =
			stringTrim (
				orignalSource);

		if (
			stringContains (
				" to ",
				source)
		) {

			List <String> parts =
				stringSplitSimple (
					" to ",
					source);

			if (
				collectionDoesNotHaveTwoElements (
					parts)
			) {
				return optionalAbsent ();
			}

			Optional <Pair <Interval, String>> optionalFirstInterval =
				parsePartial (
					timezone,
					parts.get (0).trim (),
					hourOffset);

			if (
				optionalIsNotPresent (
					optionalFirstInterval)
			) {
				return Optional.absent ();
			}

			Optional <Pair <Interval, String>> optionalSecondInterval =
				parsePartial (
					timezone,
					parts.get (1).trim (),
					hourOffset);

			if (
				optionalIsNotPresent (
					optionalSecondInterval)
			) {
				return optionalAbsent ();
			}

			Interval interval =
				new Interval (
					optionalFirstInterval.get ().getLeft ().getStart (),
					optionalSecondInterval.get ().getLeft ().getEnd ());

			return optionalOf (
				new TextualInterval (
					source.trim (),
					stringFormat (
						"%s to %s",
						optionalFirstInterval.get ().getRight (),
						optionalSecondInterval.get ().getRight ()),
					interval));

		} else if (

			stringStartsWithSimple (
				"last ",
				source)

			&& stringNotInSafe (
				source,
				"last day",
				"last month",
				"last year")

		) {

			return parseRecent (
				timezone,
				source);

		} else {

			Optional <Pair <Interval, String>> optionalInterval =
				parsePartial (
					timezone,
					source.trim (),
					hourOffset);

			if (
				optionalIsNotPresent (
					optionalInterval)
			) {
				return optionalAbsent ();
			}

			return optionalOf (
				new TextualInterval (
					source.trim (),
					optionalInterval.get ().getRight (),
					optionalInterval.get ().getLeft ()));

		}

	}

	public static
	Optional <TextualInterval> parse (
			@NonNull DateTimeZone timezone,
			@NonNull String orignalSource) {

		return parse (
			timezone,
			orignalSource,
			0l);

	}

	public static
	TextualInterval parseRequired (
			@NonNull DateTimeZone timeZone,
			@NonNull String string,
			@NonNull Long hourOffset) {

		return optionalGetRequired (
			parse (
				timeZone,
				string,
				hourOffset));

	}

	public static
	TextualInterval parseRequired (
			@NonNull DateTimeZone timeZone,
			@NonNull String string) {

		return optionalGetRequired (
			parse (
				timeZone,
				string,
				0l));

	}

	public static
	TextualInterval forInterval (
			@NonNull DateTimeZone timezone,
			@NonNull Interval interval) {

		// TODO make this cleverer

		String intervalString =
			intervalToString (
				timezone,
				interval);

		return new TextualInterval (
			intervalString,
			intervalString,
			interval);

	}

	public static
	TextualInterval after (
			@NonNull DateTimeZone timezone,
			@NonNull ReadableInstant startTime) {

		return forInterval (
			timezone,
			new Interval (
				startTime,
				millisToInstant (
					Long.MAX_VALUE)));

	}

	public static
	TextualInterval before (
			@NonNull DateTimeZone timezone,
			@NonNull ReadableInstant endTime) {

		return forInterval (
			timezone,
			new Interval (
				millisToInstant (
					Long.MIN_VALUE),
				endTime));

	}

	public static
	TextualInterval forInterval (
			@NonNull DateTimeZone timezone,
			@NonNull LocalDate startDate,
			@NonNull LocalDate endDate) {

		return forInterval (
			timezone,
			new Interval (
				startDate.toDateTimeAtStartOfDay (
					timezone),
				endDate.toDateTimeAtStartOfDay (
					timezone)));

	}

	public static
	TextualInterval forInterval (
			@NonNull DateTimeZone timezone,
			@NonNull ReadableInstant start,
			@NonNull ReadableInstant end) {

		return forInterval (
			timezone,
			new Interval (
				start,
				end));

	}

	public static
	Optional <TextualInterval> forInterval (
			@NonNull DateTimeZone timezone,
			@NonNull Optional <Interval> interval) {

		if (
			optionalIsPresent (
				interval)
		) {

			return Optional.of (
				forInterval (
					timezone,
					interval.get ()));

		} else {

			return Optional.absent ();

		}

	}

	public static
	String intervalToString (
			@NonNull DateTimeZone timezone,
			@NonNull Interval interval) {

		return stringFormat (
			"%s to %s",
			formatInstant (
				timezone,
				interval.getStart ()),
			formatInstant (
				timezone,
				interval.getEnd ()));

	}

	public static
	String formatInstant (
			@NonNull DateTimeZone timezone,
			@NonNull ReadableInstant instant) {

		DateTime dateTime =
			new DateTime (
				instant,
				timezone);

		return dateTime.toString (
			timestampFormat);

	}

	// accessors

	public
	DateTime start () {
		return value.getStart ();
	}

	public
	boolean hasStart () {

		return integerNotEqualSafe (
			value.getStartMillis (),
			Long.MIN_VALUE);

	}

	public
	DateTime end () {
		return value.getEnd ();
	}

	public
	boolean hasEnd () {

		return integerNotEqualSafe (
			value.getEndMillis (),
			Long.MAX_VALUE);

	}

	// readable interval duration

	@Override
	public
	Chronology getChronology () {
		return value.getChronology ();
	}

	@Override
	public
	long getStartMillis () {
		return value.getStartMillis ();
	}

	@Override
	public
	DateTime getStart () {
		return value.getStart ();
	}

	@Override
	public
	long getEndMillis () {
		return value.getEndMillis ();
	}

	@Override
	public
	DateTime getEnd () {
		return value.getEnd ();
	}

	@Override
	public
	boolean contains (
			@NonNull ReadableInstant instant) {

		return value.contains (
			instant);

	}

	@Override
	public
	boolean contains (
			@NonNull ReadableInterval interval) {

		return value.contains (
			interval);

	}

	@Override
	public
	boolean overlaps (
			@NonNull ReadableInterval interval) {

		return value.overlaps (
			interval);

	}

	@Override
	public
	boolean isAfter (
			@NonNull ReadableInstant instant) {

		return value.isAfter (
			instant);

	}

	@Override
	public
	boolean isAfter (
			@NonNull ReadableInterval interval) {

		return value.isAfter (
			interval);

	}

	@Override
	public
	boolean isBefore (
			@NonNull ReadableInstant instant) {

		return value.isBefore (
			instant);

	}

	@Override
	public
	boolean isBefore (
			@NonNull ReadableInterval interval) {

		return value.isBefore (
			interval);

	}

	@Override
	public
	Interval toInterval () {
		return value;
	}

	@Override
	public
	MutableInterval toMutableInterval () {
		return value.toMutableInterval ();
	}

	@Override
	public
	Duration toDuration () {
		return value.toDuration ();
	}

	@Override
	public
	long toDurationMillis () {
		return value.toDurationMillis ();
	}

	@Override
	public
	Period toPeriod () {
		return value.toPeriod ();
	}

	@Override
	public
	Period toPeriod (
			@NonNull PeriodType type) {

		return value.toPeriod (
			type);

	}

	// data

	private final static
	DateTimeFormatter timestampFormat =
		DateTimeFormat

		.forPattern (
			"yyyy-MM-dd HH:mm:ss");

	public final static
	DateTimeFormatter dateFormat =
		DateTimeFormat

		.forPattern (
			"yyyy-MM-dd");

	public final static
	DateTimeFormatter monthFormat =
		DateTimeFormat

		.forPattern (
			"yyyy-MM");

	private final static
	List <Pattern> partialPatterns =
		ImmutableList.<Pattern>of (

		Pattern.compile (
			"([0-9]{4})-(0?[1-9]|1[0-2])-(0?[1-9]|[12][0-9]|3[01]) " +
			"([01][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])"),

		Pattern.compile (
			"([0-9]{4})-(0?[1-9]|1[0-2])-(0?[1-9]|[12][0-9]|3[01]) " +
			"([01][0-9]|2[0-3]):([0-5][0-9])"),

		Pattern.compile (
			"([0-9]{4})-(0?[1-9]|1[0-2])-(0?[1-9]|[12][0-9]|3[01]) " +
			"([01][0-9]|2[0-3])"),

		Pattern.compile (
			"([0-9]{4})-(0?[1-9]|1[0-2])-(0?[1-9]|[12][0-9]|3[01])"),

		Pattern.compile (
			"([0-9]{4})-(0?[1-9]|1[0-2])"),

		Pattern.compile (
			"([0-9]{4})"),

		Pattern.compile (
			"")

	);

}
