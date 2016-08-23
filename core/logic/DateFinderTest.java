package wbs.sms.core.logic;

import static wbs.framework.utils.etc.OptionalUtils.isNotPresent;
import static wbs.framework.utils.etc.OptionalUtils.isPresent;
import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.TimeUtils.localDateNotEqual;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import junit.framework.TestCase;
import lombok.Value;
import lombok.experimental.Accessors;

@RunWith (Parameterized.class)
public
class DateFinderTest
	extends TestCase {

	Example example;

	public
	DateFinderTest (
			Example example) {

		this.example =
			example;

	}

	@Test
	public
	void test () {

		Optional <LocalDate> actual =
			DateFinder.find (
				example.input (),
				example.baseYear ());

		if (

			isNotPresent (
				example.result ())

			&& isPresent (
				actual)

		) {

			fail (
				stringFormat (
					"Expected '%s' ",
					example.input (),
					"to fail but matched '%s' ",
					actual.get ().toString (),
					"(base year %s)",
					example.baseYear ()));

		}

		if (

			isPresent (
				example.result ())

			&& isNotPresent (
				actual)

		) {

			fail (
				stringFormat (
					"Expected '%s' ",
					example.input (),
					"to find '%s' ",
					example.result ().get ().toString (),
					"but failed (base year %s)",
					example.baseYear ()));

		}

		if (
			localDateNotEqual (
				example.result ().get (),
				actual.get ())
		) {

			fail (
				stringFormat (
					"Expected '%s' ",
					example.input (),
					"to find '%s' ",
					example.result ().get ().toString (),
					"but found '%s' ",
					actual.get ().toString (),
					"(base year %s)",
					example.baseYear ()));

		}

	}

	static
	LocalDate date (
			int year,
			int month,
			int day) {

		return new LocalDate (
			year,
			month,
			day);

	}

	@Accessors (fluent = true)
	@Value
	static
	class Example {
		String input;
		Integer baseYear;
		Optional<LocalDate> result;
	}

	static
	Example passingExample (
			String input,
			Integer baseYear,
			LocalDate result) {

		return new Example (
			input,
			baseYear,
			Optional.of (result));

	}

	static
	Example failingExample (
			String input) {

		return new Example (
			input,
			1900,
			Optional.absent ());

	}

	@Parameters
	public static
	List<Object[]> testCases () {

		List<Example> simpleExamples =
			ImmutableList.<Example>of (

			// ----- origin 1900

			passingExample (
				"1/10/10",
				1900,
				date (1910, 10, 1)),

			passingExample (
				"1 nov 1980",
				1900,
				date (1980, 11, 1)),

			passingExample (
				"This has a hidden date 1.jan.1930, blah blah",
				1900,
				date (1930, 1, 1)),

			passingExample (
				"16 . 12 . 80",
				1900,
				date (1980, 12, 16)),

			passingExample (
				"160269",
				1900,
				date (1969, 2, 16)),

			passingExample (
				"03.2.81",
				1900,
				date (1981, 2, 3)),

			passingExample (
				"05 .08.1970",
				1900,
				date (1970, 8, 5)),

			passingExample (
				"18th oct.1975",
				1900,
				date (1975, 10, 18)),

			passingExample (
				"05Feb1975 ",
				1900,
				date (1975, 2, 5)),

			passingExample (
				"24.1O.65.",
				1900,
				date (1965, 10, 24)), // letter 'O' not digit '0'

			passingExample (
				"I0-II-I952",
				1900,
				date (1952, 11, 10)), // letter 'I' not digit '1'

			passingExample (
				"26 l l954",
				1900,
				date (1954, 1, 26)), // letter 'l' not digit '1'

			passingExample (
				"28 April 1995",
				1900,
				date (1995, 4, 28)),

			passingExample (
				"Diana Ross June 27 1966",
				1900,
				date (1966, 6, 27)),

			passingExample (
				"Mandy parke 9 of march 1981",
				1900,
				date (1981, 3, 9)),

			passingExample (
				"a01/01/01",
				1900,
				date (1901, 1, 1)),

			passingExample (
				"01/01/01a",
				1900,
				date (1901, 1, 1)),

			passingExample (
				"Date of birth9 8 48 my name is sarah",
				1900,
				date (1948, 8, 9)),

			passingExample (
				"14_07_1987 nonna",
				1900,
				date (1987, 7, 14)),

			passingExample (
				"Kim 11of Feb 1975",
				1900,
				date (1975, 2, 11)),

			passingExample (
				"12th of febuary 1997 and silly tack",
				1900,
				date (1997, 2, 12)),

			passingExample (
				"02/19/1965. Florian",
				1900,
				date (1965, 2, 19)),

			passingExample (
				"Sheridon 8th of February 1980",
				1900,
				date (1980, 2, 8)),

			passingExample (
				"Bonnie angell 10/12/1996",
				1900,
				date (1996, 12, 10)),

			// ----- origin 1950

			passingExample (
				"1/10/10",
				1950,
				date (2010, 10, 1)),

			passingExample (
				"1/1/50",
				1950,
				date (1950, 1, 1)),

			// ----- failing

			failingExample (
				"This is not a valid date 32 jan 2003"),

			failingExample (
				"13/13/1900"),

			failingExample (
				"01001900"),

			failingExample (
				"30 feb 1950")

		);

		return ImmutableList.<Example>builder ()

			.addAll (
				simpleExamples)

			.addAll (
				IntStream.rangeClosed (1, 31)

				.mapToObj (day ->
					passingExample (
						stringFormat (
							"%s/01/1980",
							day),
						1900,
						date (1980, 1, day)))

				.iterator ())

			.addAll (
				IntStream.rangeClosed (1, 31)

				.mapToObj (day ->
					passingExample (
						stringFormat (
							"%s%s/01/1980",
							day,
							suffixes.get (day)),
						1900,
						date (1980, 1, day)))

				.iterator ())

			.build ()

			.stream ()

			.map (object -> new Object [] { object })

			.collect (Collectors.toList ());

	}

	static
	Map<Integer,String> suffixes =
		ImmutableMap.<Integer,String>builder ()
			.put (1, "st")
			.put (2, "nd")
			.put (3, "rd")
			.put (4, "th")
			.put (5, "th")
			.put (6, "th")
			.put (7, "th")
			.put (8, "th")
			.put (9, "th")
			.put (10, "th")
			.put (11, "th")
			.put (12, "th")
			.put (13, "th")
			.put (14, "th")
			.put (15, "th")
			.put (16, "th")
			.put (17, "th")
			.put (18, "th")
			.put (19, "th")
			.put (20, "th")
			.put (21, "st")
			.put (22, "nd")
			.put (23, "rd")
			.put (24, "th")
			.put (25, "th")
			.put (26, "th")
			.put (27, "th")
			.put (28, "th")
			.put (29, "th")
			.put (30, "th")
			.put (31, "st")
			.build ();

}
