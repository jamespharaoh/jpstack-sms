package wbs.sms.core.logic;

import static wbs.framework.utils.etc.Misc.in;
import static wbs.framework.utils.etc.Misc.joinWithSeparator;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import lombok.NonNull;

import org.joda.time.LocalDate;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

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

		Pattern pattern;

		DateMatcher (
				Pattern newPattern) {

			pattern = newPattern;

		}

		DateMatcher (
				String newPatternString)
			throws PatternSyntaxException {

			this (
				Pattern.compile (
					newPatternString));

		}

		DateMatcher (
				String newPatternString,
				int newPatternFlags)
			throws PatternSyntaxException {

			this (
				Pattern.compile (
					newPatternString,
					newPatternFlags));

		}

		public
		LocalDate getDate (
				Matcher matcher,
				int origin) {

			int year =
				stringToYear (
					matcher.group (3),
					origin);

			int month =
				stringToMonth (
					matcher.group (2));

			int date =
				parseInt (
					stripSuffix (
						matcher.group (1)));

			return new LocalDate (
				year,
				month,
				date);

		}

		public
		LocalDate getDate (
				String input,
				int origin) {

			Matcher matcher =
				pattern.matcher (input);

			if (! matcher.find ())
				return null;

			return getDate (
				matcher,
				origin);

		}

	}

	static
	String dayOfMonthRegexp =
		joinWithSeparator (
			"|",
			"[0oO23]?[1lI](?:st)?",
			"[0oO2]?2(?:nd)?",
			"[0oO2]?3(?:rd)?",
			"[0oO]?[4-9](?:th)?",
			"[1lI][0oO1lI23456789](?:th)?",
			"2[0oO456789](?:th)?",
			"3[0oO](?:th)?");

	static
	String monthNameRegexp =
		joinWithSeparator (
			"|",
			"jan",
			"january",
			"feb",
			"february",
			"mar",
			"march",
			"apr",
			"april",
			"may",
			"jun",
			"june",
			"jul",
			"july",
			"aug",
			"august",
			"sep",
			"sept",
			"september",
			"oct",
			"october",
			"nov",
			"november",
			"dec",
			"december");

	static
	Collection<DateMatcher> dateMatchers =
		ImmutableList.<DateMatcher>of (

			new DateMatcher (
				"\\b" +
				"(" + dayOfMonthRegexp + ")" +
				"\\W+" +
				"([0oO]?[1lI23456789]|[1lI][0oO1lI2])" +
				"\\W+" +
				"([0oO1lI23456789]{4}|[0oO1lI23456789]{2})" +
				"\\b"),

			new DateMatcher (
				"\\b" +
				"(" + dayOfMonthRegexp + ")" +
				"\\W+" +
				"(" + monthNameRegexp + ")" +
				"\\W+" +
				"([0oO1lI23456789]{4}|[0oO1lI23456789]{2})" +
				"\\b",
				Pattern.CASE_INSENSITIVE),

			new DateMatcher (
				"\\b" +
				"(" + dayOfMonthRegexp + ")" +
				"(" + monthNameRegexp + ")" +
				"(\\d{4}|\\d{2})" +
				"\\b",
				Pattern.CASE_INSENSITIVE),

			new DateMatcher (
				"\\b" +
				"(" + dayOfMonthRegexp + ")" +
				"([0oO][1lI23456789]|[1lI][0oO1lI2])" +
				"([0oO1lI23456789]{4}|[0oO1lI23456789]{2})" +
				"\\b"));

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

	private final static
	Pattern monthNamesPattern =
		Pattern.compile (
			"(jan|january|feb|february|mar|march|apr|april|may|jun|june|jul"
				+ "|july|aug|august|sep|sept|september|oct|october|nov"
				+ "|november|dec|december)",
			Pattern.CASE_INSENSITIVE);

	private final static
	Map<String,Integer> stringToMonth =
		ImmutableMap.<String,Integer>builder ()
			.put ("jan", 1)
			.put ("feb", 2)
			.put ("mar", 3)
			.put ("apr", 4)
			.put ("may", 5)
			.put ("jun", 6)
			.put ("jul", 7)
			.put ("aug", 8)
			.put ("sep", 9)
			.put ("oct", 10)
			.put ("nov", 11)
			.put ("dec", 12)
			.build ();

	public static
	Integer stringToMonth (
			String string) {

		if (monthDigitsPattern.matcher (string).matches ()) {

			return parseInt (string);

		}

		if (monthNamesPattern.matcher (string).matches ()) {

			return stringToMonth.get (
				string
					.substring (0, 3)
					.toLowerCase ());

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
