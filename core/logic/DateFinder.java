package wbs.sms.core.logic;

import static wbs.framework.utils.etc.Misc.contains;
import static wbs.framework.utils.etc.Misc.in;
import static wbs.framework.utils.etc.Misc.joinWithPipe;
import static wbs.framework.utils.etc.Misc.joinWithoutSeparator;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.NonNull;

import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.IllegalFieldValueException;
import org.joda.time.LocalDate;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

/**
 * Contains utility function to search a string for a date, in various formats.
 */
public
class DateFinder {

	// ========================================================== date matchers

	private static
	int parseInt (
			String input) {

		char[] characters =
			new char [input.length ()];

		for (
			int position = 0;
			position < input.length ();
			position ++
		) {

			char character =
				input.charAt (position);

			switch (character) {

				case 'o':
				case 'O':
					characters [position] = '0';
					break;

				case 'l':
				case 'I':
					characters [position] = '1';
					break;

				default:
					characters [position] = character;

			}

		}

		return Integer.parseInt (
			new String (characters));

	}

	static
	class DateMatcher {

		DateStyle dateStyle;
		Pattern pattern;

		DateMatcher (
				DateStyle dateStyle,
				Pattern newPattern) {

			this.dateStyle =
				dateStyle;

			pattern =
				newPattern;

		}

		DateMatcher (
				DateStyle dateStyle,
				String... newPatternStringParts)
			throws PatternSyntaxException {

			this (
				dateStyle,
				Pattern.compile (
					joinWithoutSeparator (
						newPatternStringParts),
					Pattern.CASE_INSENSITIVE));

		}

		public
		Optional<LocalDate> getDate (
				Matcher matcher,
				int origin) {

			int yearIndex;
			int monthIndex;
			int dateIndex;

			switch (dateStyle) {

			case dmy:

				dateIndex = 1;
				monthIndex = 2;
				yearIndex = 3;

				break;

			case mdy:

				monthIndex = 1;
				dateIndex = 2;
				yearIndex = 3;

				break;

			default:

				throw new RuntimeException ();

			}

			int year =
				stringToYear (
					matcher.group (
						yearIndex),
					origin);

			int month =
				stringToMonth (
					matcher.group (
						monthIndex));

			int date =
				parseInt (
					stripSuffix (
						matcher.group (
							dateIndex)));

			try {

				return Optional.of (
					new LocalDate (
						year,
						month,
						date));

			} catch (IllegalFieldValueException exception) {

				return Optional.absent ();

			}

		}

		public
		LocalDate getDate (
				String input,
				int origin) {

			Matcher matcher =
				pattern.matcher (input);

			if (! matcher.find ())
				return null;

			Optional<LocalDate> result =
				getDate (
					matcher,
					origin);

			return result.orNull ();

		}

	}

	static
	String numericDayOfMonthRegexp =
		joinWithoutSeparator (
			"(",
			joinWithPipe (
				"[0oO23]?[1lI]",
				"[0oO2]?2",
				"[0oO2]?3",
				"[0oO]?[4-9]",
				"[1lI][0oO1lI23456789]",
				"2[0oO456789]",
				"3[0oO]"),
			")");

	static
	String dayOfMonthRegexp =
		joinWithoutSeparator (
			"(",
			joinWithPipe (
				"[0oO23]?[1lI](?:st)?",
				"[0oO2]?2(?:nd)?",
				"[0oO2]?3(?:rd)?",
				"[0oO]?[4-9](?:th)?",
				"[1lI][0oO1lI23456789](?:th)?",
				"2[0oO456789](?:th)?",
				"3[0oO](?:th)?"),
			")");

	@SuppressWarnings ("unchecked")
	static
	List<List<String>> monthNames =
		ImmutableList.of (
			ImmutableList.of (
				"jan",
				"january"),
			ImmutableList.of (
				"feb",
				"february",
				"febuary"),
			ImmutableList.of (
				"mar",
				"march"),
			ImmutableList.of (
				"apr",
				"april"),
			ImmutableList.of (
				"may"),
			ImmutableList.of (
				"jun",
				"june"),
			ImmutableList.of (
				"jul",
				"july"),
			ImmutableList.of (
				"aug",
				"august"),
			ImmutableList.of (
				"sep",
				"sept",
				"september"),
			ImmutableList.of (
				"oct",
				"october"),
			ImmutableList.of (
				"nov",
				"november"),
			ImmutableList.of (
				"dec",
				"december"));

	static
	List<String> allMonthNames =
		monthNames.stream ()
			.flatMap (List::stream)
			.collect (Collectors.toList ());

	static
	Map<String,Integer> monthsByName =
		IntStream.range (0, monthNames.size ())
			.mapToObj (index ->
				monthNames.get (index).stream ()
					.map (monthName -> Pair.of (monthName, index + 1))
					.collect (Collectors.toList ()))
			.flatMap (List::stream)
			.collect (Collectors.toMap (Pair::getKey, Pair::getRight));

	static
	String monthNameRegexp =
		joinWithoutSeparator (
			"(",
			joinWithPipe (
				allMonthNames),
			")");

	final static
	String beforeDigitRegexp =
		joinWithoutSeparator (
			"(?:",
			joinWithPipe (
				"\\b",
				"\\D"),
			")");

	final static
	String afterDigitRegexp =
		joinWithoutSeparator (
			"(?:",
			joinWithPipe (
				"\\b",
				"\\D"),
			")");

	final static
	String separatorRegexp =
		joinWithoutSeparator (
			"\\P{Alnum}+");

	static
	Collection<DateMatcher> dateMatchers =
		ImmutableList.<DateMatcher>of (

		// european

		new DateMatcher (
			DateStyle.dmy,
			beforeDigitRegexp,
			dayOfMonthRegexp,
			separatorRegexp,
			"([0oO]?[1lI23456789]|[1lI][0oO1lI2])",
			separatorRegexp,
			"([0oO1lI23456789]{4}|[0oO1lI23456789]{2})",
			afterDigitRegexp),

		new DateMatcher (
			DateStyle.dmy,
			beforeDigitRegexp,
			dayOfMonthRegexp,
			separatorRegexp,
			monthNameRegexp,
			separatorRegexp,
			"([0oO1lI23456789]{4}|[0oO1lI23456789]{2})",
			afterDigitRegexp),

		new DateMatcher (
			DateStyle.dmy,
			beforeDigitRegexp,
			dayOfMonthRegexp,
			"\\P{Alnum}+of\\P{Alnum}+",
			monthNameRegexp,
			separatorRegexp,
			"([0oO1lI23456789]{4}|[0oO1lI23456789]{2})",
			afterDigitRegexp),

		new DateMatcher (
			DateStyle.dmy,
			beforeDigitRegexp,
			dayOfMonthRegexp,
			monthNameRegexp,
			"(\\d{4}|\\d{2})",
			afterDigitRegexp),

		new DateMatcher (
			DateStyle.dmy,
			beforeDigitRegexp,
			numericDayOfMonthRegexp,
			"\\P{Alnum}*of\\P{Alnum}+",
			monthNameRegexp,
			separatorRegexp,
			"([0oO1lI23456789]{4}|[0oO1lI23456789]{2})",
			afterDigitRegexp),

		new DateMatcher (
			DateStyle.dmy,
			beforeDigitRegexp,
			dayOfMonthRegexp,
			"([0oO][1lI23456789]|[1lI][0oO1lI2])",
			"([0oO1lI23456789]{4}|[0oO1lI23456789]{2})",
			afterDigitRegexp),

		// american

		new DateMatcher (
			DateStyle.mdy,
			beforeDigitRegexp,
			"([0oO]?[1lI23456789]|[1lI][0oO1lI2])",
			separatorRegexp,
			dayOfMonthRegexp,
			separatorRegexp,
			"([0oO1lI23456789]{4}|[0oO1lI23456789]{2})",
			afterDigitRegexp),

		new DateMatcher (
			DateStyle.mdy,
			"\\b",
			monthNameRegexp,
			separatorRegexp,
			dayOfMonthRegexp,
			separatorRegexp,
			"(\\d{4}|\\d{2})",
			afterDigitRegexp),

		new DateMatcher (
			DateStyle.mdy,
			"\\b",
			monthNameRegexp,
			dayOfMonthRegexp,
			separatorRegexp,
			"(\\d{4}|\\d{2})",
			afterDigitRegexp)

	);

	static
	enum DateStyle {
		dmy,
		mdy;
	}

	/** Takes off the 'st' or whatever from a number. */
	private static String stripSuffix(String source) {
		int len = source.length();
		if (len < 2)
			return source;
		if (in(source.substring(len - 2).toLowerCase(), "st", "nd", "rd", "th"))
			return source.substring(0, len - 2);
		return source;
	}

	// ====================================================== utility functions

	public static
	int yearAdjust (
			int year,
			int origin) {

		if (year < origin % 100)
			year += 100;

		year +=
			origin - origin % 100;

		return year;

	}

	public static
	Integer stringToYear (
			String string,
			int origin) {

		if (string.length () != 2
				&& string.length () != 4) {

			throw new IllegalArgumentException ();

		}

		int year =
			parseInt (string);

		if (string.length () == 2) {

			year =
				yearAdjust (
					year,
					origin);

		}

		return year;

	}

	private final static
	Pattern monthDigitsPattern =
		Pattern.compile (
			"[0oO]?[1lI23456789]|[1lI][0oO1lI2]");

	public static
	Integer stringToMonth (
			String string) {

		if (monthDigitsPattern.matcher (string).matches ()) {

			return parseInt (string);

		}

		if (
			contains (
				monthsByName,
				string.toLowerCase ())
		) {

			return monthsByName.get (
				string.toLowerCase ());

		}

		throw new IllegalArgumentException ();

	}

	// =================================================================== find

	public static
	Optional<LocalDate> find (
			@NonNull String input,
			@NonNull Integer origin) {

		for (
			DateMatcher dateMatcher
				: dateMatchers
		) {

			LocalDate date =
				dateMatcher.getDate (
					input,
					origin);

			if (date != null) {

				return Optional.of (
					date);

			}

		}

		return Optional.absent ();

	}

	// ============================================================ constructor

	private DateFinder() {
		// never instantiated
	}
}
