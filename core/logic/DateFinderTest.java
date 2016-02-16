package wbs.sms.core.logic;

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.joda.time.LocalDate;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public
class DateFinderTest
	extends TestCase {

	static
	Optional<LocalDate> date (
			int year,
			int month,
			int day) {

		return Optional.of (
			new LocalDate (
				year,
				month,
				day));

	}

	/**
	 * These are mostly real examples from the chat system.
	 */
	Map<Integer,Map<String,Optional<LocalDate>>> dateTests =
		ImmutableMap.<Integer,Map<String,Optional<LocalDate>>>builder ()

		.put (
			1900,
			ImmutableMap.<String,Optional<LocalDate>>builder ()

			.put (
				"1/10/10",
				date (1910, 10, 1))

			.put (
				"1 nov 1980",
				date (1980, 11, 1))

			.put (
				"This has a hidden date 1.jan.1930, blah blah",
				date (1930, 1, 1))

			.put (
				"16 . 12 . 80",
				date (1980, 12, 16))

			.put (
				"160269",
				date (1969, 2, 16))

			.put (
				"03.2.81",
				date (1981, 2, 3))

			.put (
				"05 .08.1970",
				date (1970, 8, 5))

			.put (
				"18th oct.1975",
				date (1975, 10, 18))

			.put (
				"05Feb1975 ",
				date (1975, 2, 5))

			.put (
				"24.1O.65.",
				date (1965, 10, 24)) // letter 'O' not digit '0'

			.put (
				"I0-II-I952",
				date (1952, 11, 10)) // letter 'I' not digit '1'

			.put (
				"26 l l954",
				date (1954, 1, 26)) // letter 'l' not digit '1'

			.put (
				"28 April 1995",
				date (1995, 4, 28))

			.build ())

		.put (
			1950,
			ImmutableMap.<String,Optional<LocalDate>>builder ()

			.put (
				"1/10/10",
				date (2010, 10, 1))

			.put (
				"1/1/50",
				date (1950, 1, 1))

			.build ())

		.build ();

	public
	void testDateFinder () {

		// do the tests specified above

		for (
			Map.Entry<Integer,Map<String,Optional<LocalDate>>> ent1
				: dateTests.entrySet ()
		) {

			int origin =
				ent1.getKey ();

			for (
				Map.Entry<String,Optional<LocalDate>> ent2
					: ent1.getValue ().entrySet ()
			) {

				assertEquals (
					ent2.getValue (),
					DateFinder.find (
						ent2.getKey (),
						origin));

			}

		}

		// now test every day of the month, with and without suffixes (eg 'st')

		for (int i = 1; i <= 31; i++) {

			String suffix = "";

			if (i == 1 || i == 21 || i == 31)
				suffix = "st";
			else if (i == 2 || i == 22)
				suffix = "nd";
			else if (i == 3 || i == 23)
				suffix = "rd";
			else
				suffix = "th";

			String text1 =
				"" + i + "/01/1980";

			String text2 =
				"" + i + suffix + "/01/1980";

			Optional<LocalDate> date =
				date (1980, 1, i);

			assertEquals (
				date,
				DateFinder.find (
					text1,
					1900));

			assertEquals (
				date,
				DateFinder.find (
					text2,
					1900));

		}

	}

	List<String> invalidDateTests =
		ImmutableList.<String>of (
			"This is not a valid date 32 jan 2003",
			"12/13/06",
			"a01/01/01",
			"01/01/01a");

	public
	void testDateFinderInvalids () {

		for (
			String string
				: invalidDateTests
		) {

			assertNull (
				DateFinder.find (
					string,
					1950
				).orNull ());

		}

	}

}
