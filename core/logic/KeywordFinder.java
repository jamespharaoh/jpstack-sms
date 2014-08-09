package wbs.sms.core.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Data;
import lombok.experimental.Accessors;
import wbs.sms.gsm.Gsm;

/**
 * Utility class to extract a keyword from the start of a string.
 */
public
class KeywordFinder {

	final static
	String keywordCharsNoNums =
		"\\p{Lu}\\p{Ll}\\p{Lt}\\p{Lm}\\p{Lo}";

	final static
	String keywordChars =
		keywordCharsNoNums + "0-9";

	// match two words as one

	final static
	Pattern keywordPattern1 =
		Pattern.compile (
			"([^" + keywordChars + "]*)" +
			"([" + keywordChars + "]+" +
			"[^" + keywordChars + "]+" +
			"[" + keywordChars + "]+)" +
			"(.*)",
			Pattern.DOTALL);

	// match a single word

	final static
	Pattern keywordPattern2 =
		Pattern.compile (
			"([^" + keywordChars + "]*)" +
			"([" + keywordChars + "]+)" +
			"(.*)",
			Pattern.DOTALL);

	// match a single word, not including trailing numbers

	final static
	Pattern keywordPattern3 =
		Pattern.compile (
			"([^" + keywordChars + "]*)" +
			"([" + keywordChars + "]*" +
			"[" + keywordCharsNoNums + "])" +
			"([0-9]+.*)",
			Pattern.DOTALL);

	private
	KeywordFinder () {
		// never instantiated
	}

	/**
	 * Produces a collection of possible keyword matches from the given string.
	 */
	public static
	List<Match> find (
			String string) {

		List<Match> ret =
			new ArrayList<Match> ();

		// try first pattern

		Matcher matcher1 =
			keywordPattern1.matcher (
				string);

		if (matcher1.matches ()) {

			ret.add (
				match (
					matcher1.group (2),
					matcher1.group (3)));

		}

		// try second pattern

		Matcher matcher2 =
			keywordPattern2.matcher (
				string);

		if (matcher2.matches ()) {

			ret.add (
				match (
					matcher2.group (2),
					matcher2.group (3)));

		}

		// try third pattern

		Matcher matcher3 =
			keywordPattern3.matcher (
				string);

		if (matcher3.matches ()) {

			ret.add (
				match (
					matcher3.group (2),
					matcher3.group (3)));

		}

		// return

		return ret;

	}

	final static
	Pattern shitPattern =
		Pattern.compile (
			"[^" + keywordChars + "]+");

	static
	String stripKeyword (
			String string) {

		return Gsm.toSimpleAlpha (
			shitPattern
				.matcher (string)
				.replaceAll (""));

	}

	static
	Match match (
			String keyword,
			String rest) {

		return new Match ()

			.keyword (
				keyword)

			.simpleKeyword (
				stripKeyword (keyword))

			.rest (
				rest.trim ());

	}

	@Accessors (fluent = true)
	@Data
	public static
	class Match {

		String keyword;
		String simpleKeyword;
		String rest;

	}

}
