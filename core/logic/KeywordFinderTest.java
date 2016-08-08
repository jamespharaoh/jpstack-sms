package wbs.sms.core.logic;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public
class KeywordFinderTest
	extends TestCase {

	Map<String,List<List<String>>> tests =
		ImmutableMap.<String,List<List<String>>>builder ()

		.put (
			"buzz10.12.74. chris armstrong",
			ImmutableList.<List<String>>of (

				ImmutableList.<String>of (
					"buzz10.12",
					"buzz1012",
					".74. chris armstrong"),

				ImmutableList.<String>of (
					"buzz10",
					"buzz10",
					".12.74. chris armstrong"),

				ImmutableList.<String>of (
					"buzz",
					"buzz",
					"10.12.74. chris armstrong")))

		.put (
			"STOP DAILY",
			ImmutableList.<List<String>>of (

				ImmutableList.<String>of (
					"STOP DAILY",
					"stopdaily",
					""),

				ImmutableList.<String>of (
					"STOP",
					"stop",
					"DAILY")))

		.put (
			"jamie ring me",
			ImmutableList.<List<String>>of (

				ImmutableList.<String>of (
					"jamie ring",
					"jamiering",
					"me"),

				ImmutableList.<String>of (
					"jamie",
					"jamie",
					"ring me")))

		.put (
			"Vote1",
			ImmutableList.<List<String>>of (

				ImmutableList.<String>of (
					"Vote1",
					"vote1",
					""),

				ImmutableList.<String>of (
					"Vote",
					"vote",
					"1")))

		.put (
			"Vote1 2 3 4",
			ImmutableList.<List<String>>of (

				ImmutableList.<String>of (
					"Vote1 2",
					"vote12",
					"3 4"),

				ImmutableList.<String>of (
					"Vote1",
					"vote1",
					"2 3 4"),

				ImmutableList.<String>of (
					"Vote",
					"vote",
					"1 2 3 4")))

		.put (
			"VOTE(2)",
			ImmutableList.<List<String>>of (

				ImmutableList.<String>of (
					"VOTE(2",
					"vote2",
					")"),

				ImmutableList.<String>of (
					"VOTE",
					"vote",
					"(2)")))

		.put (
			"Where.have.you.done.it",
			ImmutableList.<List<String>>of (

				ImmutableList.<String>of (
					"Where.have",
					"wherehave",
					".you.done.it"),

				ImmutableList.<String>of (
					"Where",
					"where",
					".have.you.done.it")))

		.put (
			"Text2date091080 this is me",
			ImmutableList.<List<String>>of (

				ImmutableList.<String>of (
					"Text2date091080 this",
					"text2date091080this",
					"is me"),

				ImmutableList.<String>of (
					"Text2date091080",
					"text2date091080",
					"this is me"),

				ImmutableList.<String>of (
					"Text2date",
					"text2date",
			"091080 this is me")))

		.put (
			"caf\u00e9 culture hello!",
			ImmutableList.<List<String>>of (

				ImmutableList.<String>of (
					"caf\u00e9 culture",
					"cafeculture",
					"hello!"),

				ImmutableList.<String>of (
					"caf\u00e9",
					"cafe",
					"culture hello!")))

		.put (
			"this has some\nline feeds\nand\rcarriage\rreturns",
			ImmutableList.<List<String>>of (

				ImmutableList.<String>of (
					"this has",
					"thishas",
					"some\nline feeds\nand\rcarriage\rreturns"),

				ImmutableList.<String>of (
					"this",
					"this",
					"has some\nline feeds\nand\rcarriage\rreturns")))

		.put (
			"123456",
			ImmutableList.<List<String>>of (

				ImmutableList.<String>of (
					"123456",
					"123456",
					"")))

		.put (
			"123456 hi there",
			ImmutableList.<List<String>>of (

				ImmutableList.<String>of (
					"123456 hi",
					"123456hi",
					"there"),

				ImmutableList.<String>of (
					"123456",
					"123456",
					"hi there")))

		.put (
			"st0p",
			ImmutableList.<List<String>>of (

				ImmutableList.<String>of (
					"st0p",
					"st0p",
					""),

				ImmutableList.<String>of (
					"st",
					"st",
					"0p")))

		.put (
			"s t o p",
			ImmutableList.<List<String>>of (

				ImmutableList.<String>of (
					"s t",
					"st",
					"o p"),

				ImmutableList.<String>of (
					"s",
					"s",
					"t o p")))

		.put (
			"Ts25 1bs",
			ImmutableList.<List<String>>of (

				ImmutableList.<String>of (
					"Ts25 1bs",
					"ts251bs",
					""),

				ImmutableList.<String>of (
					"Ts25",
					"ts25",
					"1bs"),

				ImmutableList.<String>of (
					"Ts",
					"ts",
					"25 1bs")))

		.build ();

	List<String> prefixes =
		ImmutableList.<String>of (
			"",
			" ",
			"  ",
			"\"",
			" \"",
			" \" ",
			"..\'",
			" (");

	public
	void testKeywordFinder () {

		for (
			Map.Entry<String,List<List<String>>> testEntry
				: tests.entrySet ()
		) {

			KeywordFinder keywordFinder =
				new KeywordFinder ();

			String baseInput =
				testEntry.getKey ();

			List<List<String>> expectedResults =
				testEntry.getValue ();

			for (String prefix : prefixes) {

				String input =
					prefix + baseInput;

				List<KeywordFinder.Match> actualResults =
					keywordFinder.find (input);

				assertEquals (
					stringFormat (
						"number of results for [%s]",
						input),
					expectedResults.size (),
					actualResults.size ());

				for (
					int index = 0;
					index < expectedResults.size ();
					index ++
				) {

					KeywordFinder.Match actualResult =
						actualResults.get (index);

					List<String> expectedResult =
						expectedResults.get (index);

					assertEquals (
						stringFormat (
							"result[%s].keyword for [%s]",
							index,
							input),
						(String) expectedResult.get (0),
						(String) actualResult.keyword ());

					assertEquals (
						stringFormat (
							"result[%s].simpleKeyword for [%s]",
							index,
							input),
						(String) expectedResult.get (1),
						(String) actualResult.simpleKeyword ());

					assertEquals (
						stringFormat (
							"result[%s].rest for [%s]",
							index,
							input),
						(String) expectedResult.get (2),
						(String) actualResult.rest ());

				}

			}

		}

	}

}
