package wbs.utils.string;

import static wbs.utils.collection.IterableUtils.iterableMap;
import static wbs.utils.etc.EnumUtils.enumNameHyphensLazy;
import static wbs.utils.etc.LogicUtils.booleanToYesNo;
import static wbs.utils.etc.Misc.lessThan;
import static wbs.utils.etc.Misc.minus;
import static wbs.utils.etc.Misc.sum;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NullUtils.nullIf;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.integerToDecimalStringLazy;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.TypeUtils.classNameFull;
import static wbs.utils.etc.TypeUtils.classNameSimple;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import lombok.NonNull;

import wbs.utils.exception.RuntimeUnsupportedEncodingException;

public
class StringUtils {

	public static
	boolean stringIsEmpty (
			@NonNull String string) {

		return string.isEmpty ();

	}

	public static
	boolean stringIsNotEmpty (
			@NonNull String string) {

		return ! string.isEmpty ();

	}

	public static
	String emptyStringIfNull (
			String string) {

		return ifNull (
			string,
			"");

	}

	public static
	String nullIfEmptyString (
			String string) {

		return nullIf (
			string,
			"");

	}

	public static
	String joinWithSeparator (
			@NonNull CharSequence separator,
			@NonNull CharSequence prefix,
			@NonNull Iterable <? extends CharSequence> parts,
			@NonNull CharSequence suffix) {

		StringBuilder stringBuilder =
			new StringBuilder ();

		boolean first = true;

		for (
			CharSequence part
				: parts
		) {

			if (first) {

				first = false;

			} else {

				stringBuilder.append (
					separator);

			}

			stringBuilder.append (
				prefix);

			stringBuilder.append (
				part);

			stringBuilder.append (
				suffix);

		}

		return stringBuilder.toString ();

	}

	public static
	String joinWithoutSeparator (
			@NonNull Iterable <? extends CharSequence> parts) {

		return joinWithSeparator (
			"",
			"",
			parts,
			"");

	}

	public static
	String joinWithoutSeparator (
			@NonNull String ... parts) {

		return joinWithSeparator (
			"",
			"",
			Arrays.asList (
				parts),
			"");

	}

	public static
	String joinWithSeparator (
			String separator,
			Iterable <? extends CharSequence> parts) {

		return joinWithSeparator (
			separator,
			"",
			parts,
			"");

	}

	public static
	String joinWithSeparator (
			@NonNull String separator,
			@NonNull String ... parts) {

		return joinWithSeparator (
			separator,
			"",
			Arrays.asList (parts),
			"");

	}

	public static
	String joinWithNewline (
			@NonNull Iterable <CharSequence> parts) {

		return joinWithSeparator (
			"\n",
			"",
			parts,
			"");

	}

	public static
	String joinWithNewline (
			@NonNull CharSequence ... parts) {

		return joinWithSeparator (
			"\n",
			"",
			Arrays.asList (
				parts),
			"");

	}

	public static
	String joinWithSpace (
			@NonNull Iterable <? extends CharSequence> parts) {

		return joinWithSeparator (
			" ",
			"",
			parts,
			"");

	}

	public static
	String joinWithSpace (
			String ... parts) {

		return joinWithSeparator (
			" ",
			"",
			Arrays.asList (
				parts),
			"");

	}

	public static
	String joinWithFullStop (
			@NonNull Iterable <? extends CharSequence> parts) {

		return joinWithSeparator (
			".",
			"",
			parts,
			"");

	}

	public static
	String joinWithFullStop (
			@NonNull String ... parts) {

		return joinWithSeparator (
			".",
			"",
			Arrays.asList (
				parts),
			"");

	}

	public static
	String joinWithSlash (
			@NonNull Iterable <? extends CharSequence> parts) {

		return joinWithSeparator (
			"/",
			"",
			parts,
			"");

	}

	public static
	String joinWithSlash (
			@NonNull String ... parts) {

		return joinWithSeparator (
			"/",
			"",
			Arrays.asList (
				parts),
			"");

	}

	public static
	String joinWithPipe (
			@NonNull Iterable <String> parts) {

		return joinWithSeparator (
			"|",
			"",
			parts,
			"");

	}

	public static
	String joinWithPipe (
			@NonNull String ... parts) {

		return joinWithSeparator (
			"|",
			"",
			Arrays.asList (
				parts),
			"");

	}

	public static
	String joinWithCommaAndSpace (
			@NonNull Iterable <? extends CharSequence> parts) {

		return joinWithSeparator (
			", ",
			"",
			parts,
			"");

	}

	public static
	String joinWithCommaAndSpace (
			@NonNull CharSequence ... parts) {

		return joinWithSeparator (
			", ",
			"",
			Arrays.asList (
				parts),
			"");

	}

	public static
	LazyString joinWithCommaAndSpaceLazy (
			@NonNull Iterable <? extends CharSequence> parts) {

		return LazyString.singleton (
			() -> joinWithSeparator (
				", ",
				"",
				parts,
				""));

	}

	public static
	LazyString joinWithCommaAndSpaceLazy (
			@NonNull CharSequence ... parts) {

		return LazyString.singleton (
			() -> joinWithSeparator (
				", ",
				"",
				Arrays.asList (
					parts),
				""));

	}

	public static
	String joinWithComma (
			@NonNull Iterable <String> parts) {

		return joinWithSeparator (
			",",
			"",
			parts,
			"");

	}

	public static
	String joinWithComma (
			@NonNull String ... parts) {

		return joinWithSeparator (
			",",
			"",
			Arrays.asList (
				parts),
			"");

	}

	public static
	String joinWithSemicolonAndSpace (
			@NonNull Iterable <String> parts) {

		return joinWithSeparator (
			"; ",
			"",
			parts,
			"");

	}

	public static
	String joinWithSemicolonAndSpace (
			@NonNull String... parts) {

		return joinWithSeparator (
			"; ",
			"",
			Arrays.asList (
				parts),
			"");

	}

	public static
	String substring (
			@NonNull String string,
			int start,
			int end) {

		return string.substring (
			start,
			end);

	}

	public static
	String substring (
			@NonNull String string,
			long start,
			long end) {

		return string.substring (
			Math.toIntExact (
				start),
			Math.toIntExact (
				end));

	}

	public static
	String substringFrom (
			@NonNull String string,
			int start) {

		return string.substring (
			start);

	}

	public static
	String substringFrom (
			@NonNull String string,
			long start) {

		return string.substring (
			Math.toIntExact (
				start));

	}

	public static
	String substringTo (
			@NonNull String string,
			int end) {

		return string.substring (
			0,
			end);

	}

	public static
	String substringTo (
			@NonNull String string,
			long end) {

		return string.substring (
			0,
			Math.toIntExact (
				end));

	}

	public static
	String hyphenToCamel (
			@NonNull String string) {

		return delimitedToCamel (
			string,
			"-");

	}

	public static
	String hyphenToCamelCapitalise (
			@NonNull String string) {

		return capitalise (
			delimitedToCamel (
				string,
				"-"));

	}

	public static
	String hyphenToSpaces (
			@NonNull String string) {

		return stringReplaceAllSimple (
			"-",
			" ",
			string);

	}

	public static
	String underscoreToCamel (
			@NonNull String string) {

		return delimitedToCamel (
			string,
			"_");

	}

	public static
	String replaceAll (
			@NonNull String source,
			@NonNull String find,
			@NonNull String replaceWith) {

		return source.replaceAll (
			Pattern.quote (
				find),
			replaceWith);

	}

	public static
	String underscoreToHyphen (
			@NonNull String string) {

		return replaceAll (
			string,
			"_",
			"-");

	}

	public static
	String underscoreToSpaces (
			@NonNull String string) {

		return replaceAll (
			string,
			"_",
			" ");

	}

	public static
	String underscoreToSpacesCapitalise (
			@NonNull String string) {

		return capitalise (
			replaceAll (
				string,
				"_",
				" "));

	}

	public static
	String hyphenToUnderscore (
			@NonNull String string) {

		return replaceAll (
			string,
			"-",
			"_");

	}

	public static
	String delimitedToCamel (
			@NonNull String string,
			@NonNull String delimiter) {

		String[] parts =
			string.split (delimiter);

		StringBuilder ret =
			new StringBuilder (parts [0]);

		for (
			int index = 1;
			index < parts.length;
			index ++
		) {

			ret.append (
				Character.toUpperCase (
					parts [index].charAt (0)));

			ret.append (
				parts [index].substring (1));

		}

		return ret.toString ();

	}

	public static
	String camelToUnderscore (
			String string) {

		return camelToDelimited (
			string,
			"_");

	}

	public static
	String camelToHyphen (
			String string) {

		return camelToDelimited (
			string,
			"-");

	}

	public static
	String camelToSpaces (
			String string) {

		return camelToDelimited (
			string,
			" ");

	}

	public static
	List <String> camelToList (
			@NonNull String string) {

		ImmutableList.Builder <String> listBuilder =
			ImmutableList.builder ();

		StringBuilder itemBuilder =
			new StringBuilder ();

		itemBuilder.append (
			Character.toLowerCase (
				string.charAt (0)));

		for (
			int position = 1;
			position < string.length ();
			position ++
		) {

			char character =
				string.charAt (
					position);

			if (
				Character.isUpperCase (
					character)
			) {

				listBuilder.add (
					itemBuilder.toString ());

				itemBuilder =
					new StringBuilder ();

				itemBuilder.append (
					Character.toLowerCase (
						character));

			} else {

				itemBuilder.append (
					character);

			}

		}

		listBuilder.add (
			itemBuilder.toString ());

		return listBuilder.build ();

	}

	public static
	String camelFromIterable (
			@NonNull Iterable <String> parts) {

		Iterator <String> iterator =
			parts.iterator ();

		if (! iterator.hasNext ()) {
			return "";
		}

		StringBuilder stringBuilder =
			new StringBuilder ();

		stringBuilder.append (
			iterator.next ());

		while (iterator.hasNext ()) {

			stringBuilder.append (
				capitalise (
					iterator.next ()));

		}

		return stringBuilder.toString ();

	}

	public static
	String camelToDelimited (
			String string,
			String delimiter) {

		if (string == null)
			return null;

		StringBuilder stringBuilder =
			new StringBuilder (
				string.length () * 2);

		stringBuilder.append (
			Character.toLowerCase (
				string.charAt (0)));

		for (
			int pos = 1;
			pos < string.length ();
			pos ++
		) {

			char ch =
				string.charAt (pos);

			if (Character.isUpperCase (ch)) {

				stringBuilder.append (
					delimiter);

				stringBuilder.append (
					Character.toLowerCase (ch));

			} else {

				stringBuilder.append (ch);

			}

		}

		return stringBuilder.toString ();

	}

	public static
	String spacify (
			String input) {

		return spacify (
			input,
			32);

	}

	private final static
	Pattern nonWhitespaceWordsPattern =
		Pattern.compile ("\\S+");

	/**
	 * Returns a transformed version of a string, with all whitespace replaced
	 * by a single space and all words longer than wordLength split into
	 * wordLength or less.
	 *
	 * @param input
	 *            the input string
	 * @param wordLength
	 *            the maximum word length in the output string
	 * @return the transformed string
	 */
	public static
	String spacify (
			String input,
			int wordLength) {

		StringBuilder stringBuilder =
			new StringBuilder ();

		Matcher matcher =
			nonWhitespaceWordsPattern.matcher (input);

		while (matcher.find ()) {

			String string =
				matcher.group (0);

			int position = 0;

			int length =
				string.length ();

			while (true) {

				if (stringBuilder.length () > 0)
					stringBuilder.append (' ');

				if (position + wordLength > length) {

					stringBuilder.append (
						string,
						position,
						length);

					break;

				}

				stringBuilder.append (
					string,
					position,
					position + wordLength);

				position += wordLength;

			}

		}

		return stringBuilder.toString ();

	}

	public static
	List <String> stringSplitRegexp (
			@NonNull String pattern,
			@NonNull String source) {

		if (source.isEmpty ()) {

			return Collections.emptyList ();

		} else {

			return Arrays.asList (
				source.split (
					pattern));

		}

	}

	public static
	List <String> stringSplitSimple (
			@NonNull String separator,
			@NonNull String source) {

		if (source.isEmpty ()) {

			return Collections.emptyList ();

		} else {

			return Arrays.asList (
				source.split (
					Pattern.quote (
						separator)));

		}

	}

	public static
	List <String> stringSplitSpace (
			@NonNull String source) {

		return stringSplitSimple (
			" ",
			source);

	}

	public static
	List <String> stringSplitComma (
			@NonNull String source) {

		return stringSplitSimple (
			",",
			source);

	}

	public static
	List <String> stringSplitColon (
			@NonNull String source) {

		return stringSplitSimple (
			":",
			source);

	}

	public static
	List <String> stringSplitFullStop (
			@NonNull String source) {

		return stringSplitSimple (
			".",
			source);

	}

	public static
	List <String> stringSplitSlash (
			@NonNull String source) {

		return stringSplitSimple (
			"/",
			source);

	}

	public static
	List <String> stringSplitHyphen (
			@NonNull String source) {

		return stringSplitSimple (
			"-",
			source);

	}

	public static
	List <String> stringSplitNewline (
			@NonNull String source) {

		return stringSplitSimple (
			"\n",
			source);

	}

	public static
	boolean doesNotStartWithSimple (
			@NonNull String string,
			@NonNull String prefix) {

		return ! string.startsWith (
			prefix);

	}

	public static
	byte[] stringToUtf8 (
			@NonNull String string) {

		return stringToBytes (
			string,
			"utf-8");

	}


	public static
	byte[] stringToBytes (
			@NonNull String string,
			@NonNull String charset) {

		try {

			return string.getBytes (
				charset);

		} catch (UnsupportedEncodingException exception) {

			throw new RuntimeException (exception);

		}

	}

	public static
	String bytesToString (
			byte[] bytes,
			String charset) {

		try {

			return new String (
				bytes,
				charset);

		} catch (UnsupportedEncodingException unsupportedEncodingException) {

			throw new RuntimeUnsupportedEncodingException (
				unsupportedEncodingException);

		}

	}

	public static
	String bytesToStringSafe (
			@NonNull byte[] bytes,
			@NonNull String charset) {

		String value =
			bytesToString (
				bytes,
				charset);

		if (
			stringInSafe (
				value,
				"\0")
		) {

			throw new IllegalArgumentException (
				"String contains null bytes");

		}

		return value;

	}

	public static
	String utf8ToString (
			@NonNull byte[] bytes) {

		return bytesToString (
			bytes,
			"utf-8");

	}

	public static
	String utf8ToStringSafe (
			@NonNull byte[] bytes) {

		return bytesToStringSafe (
			bytes,
			"utf-8");

	}

	private final static
	Pattern nonAlphanumericWordsPattern =
		Pattern.compile ("[^a-z0-9]+");

	public static
	String simplify (
			@NonNull String s) {

		return nonAlphanumericWordsPattern.matcher (
			s.toLowerCase()).replaceAll(
				" ").trim();

	}

	public static
	String objectToStringNullSafe (
			Object object) {

		if (object == null)
			return "(null)";

		return object.toString ();

	}

	public static
	String objectToString (
			@NonNull Object object) {

		return object.toString ();

	}

	public static
	boolean equalIgnoreCase (
			@NonNull String... strings) {

		if (strings.length == 0)
			return true;

		String firstString =
			strings [0];

		for (
			String string
				: strings
		) {

			if (string == firstString)
				continue;

			if (! firstString.equalsIgnoreCase (string))
				return false;

		}

		return true;

	}

	public static
	String naivePluralise (
			@NonNull String singular) {

		if (

			stringEndsWithSimple (
				"s",
				singular)

			|| stringEndsWithSimple (
				"x",
				singular)

		) {

			return singular + "es";

		} else {

			return singular + "s";

		}

	}

	public static
	boolean stringStartsWithSimple (
			@NonNull CharSequence prefix,
			@NonNull CharSequence subject) {

		return subject.toString ().startsWith (
			prefix.toString ());

	}

	public static
	boolean stringEndsWithSimple (
			@NonNull CharSequence suffix,
			@NonNull CharSequence subject) {

		return subject.toString ().endsWith (
			suffix.toString ());

	}

	public static
	String pluralise (
			long quantity,
			@NonNull String singularNoun,
			@NonNull String pluralNoun) {

		return quantity == 1L
			? "" + quantity + " " + singularNoun
			: "" + quantity + " " + pluralNoun;

	}

	public static
	String pluralise (
			long quantity,
			@NonNull String singularNoun) {

		return quantity == 1L

			? joinWithSpace (
				Long.toString (
					quantity),
				singularNoun)

			: joinWithSpace (
				Long.toString (
					quantity),
				naivePluralise (
					singularNoun));

	}

	public static
	String uppercase (
			@NonNull String string) {

		return string.toUpperCase ();

	}

	public static
	String lowercase (
			@NonNull String string) {

		return string.toLowerCase ();

	}

	public static
	String capitalise (
			@NonNull String string) {

		if (string.length () == 0)
			return "";

		return (
			Character.toUpperCase (
				string.charAt (0))
			+ string.substring (1)
		);

	}

	public static
	String capitaliseFormat (
			@NonNull CharSequence ... arguments) {

		return capitalise (
			stringFormatArray (
				arguments));

	}

	public static
	String uncapitalise (
			@NonNull String string) {

		if (string.length () == 0)
			return "";

		return (
			Character.toLowerCase (
				string.charAt (0))
			+ string.substring (1)
		);

	}

	public static
	String stringFormat (
			@NonNull Iterable <CharSequence> arguments) {

		return StringFormatter.standard (
			iterableMap (
				arguments,
				CharSequence::toString));

	}

	public static
	String stringFormat (
			@NonNull CharSequence ... arguments) {

		return StringFormatter.standard (
			iterableMap (
				Arrays.asList (
					arguments),
				CharSequence::toString));

	}

	public static
	String stringFormatArray (
			CharSequence[] arguments) {

		return StringFormatter.standard (
			iterableMap (
				Arrays.asList (
					arguments),
				CharSequence::toString));

	}

	public static
	LazyString stringFormatLazy (
			Iterable <CharSequence> arguments) {

		return LazyString.singleton (
			() -> StringFormatter.standard (
				iterableMap (
					arguments,
					CharSequence::toString)));

	}

	public static
	LazyString stringFormatLazy (
			CharSequence ... arguments) {

		return LazyString.singleton (
			() -> StringFormatter.standard (
				iterableMap (
					Arrays.asList (
						arguments),
					CharSequence::toString)));

	}

	public static
	LazyString stringFormatLazyArray (
			CharSequence[] arguments) {

		return LazyString.singleton (
			() -> StringFormatter.standard (
				iterableMap (
					Arrays.asList (
						arguments),
					CharSequence::toString)));

	}

	public static
	String fixNewlines (
			@NonNull String input) {

		return input
			.replaceAll ("\r\n", "\n")
			.replaceAll ("\r", "\n");

	}

	public static
	Optional <String> emptyStringToAbsent (
			@NonNull String source) {

		if (source.isEmpty ()) {

			return Optional.absent ();

		} else {

			return Optional.of (
				source);

		}

	}

	public static
	boolean stringEqualSafe (
			@NonNull String string0,
			@NonNull String string1) {

		return string0.equals (
			string1);

	}

	public static
	boolean stringEqualSafe (
			@NonNull CharSequence string0,
			@NonNull CharSequence string1) {

		return string0.equals (
			string1);

	}

	public static
	boolean stringNotEqualSafe (
			@NonNull String string0,
			@NonNull String string1) {

		return ! string0.equals (
			string1);

	}

	public static
	boolean stringNotEqualSafe (
			@NonNull CharSequence string0,
			@NonNull CharSequence string1) {

		return ! string0.equals (
			string1);

	}

	public static
	boolean stringLongerThan (
			@NonNull Long length,
			@NonNull String string) {

		return string.length () > length;

	}

	public static
	boolean stringNotLongerThan (
			@NonNull Long length,
			@NonNull String string) {

		return string.length () <= length;

	}

	public static
	boolean stringShorterThan (
			@NonNull Long length,
			@NonNull String string) {

		return string.length () < length;

	}

	public static
	boolean stringNotShorterThan (
			@NonNull Long length,
			@NonNull String string) {

		return string.length () >= length;

	}

	public static
	boolean stringInSafe (
			@NonNull String value,
			@NonNull Iterable <String> examples) {

		for (
			String example
				: examples
		) {

			if (
				value.equals (
					example)
			) {
				return true;
			}

		}

		return false;

	}

	public static
	boolean stringInSafe (
			@NonNull String value,
			@NonNull String... examples) {

		for (
			String example
				: examples
		) {

			if (
				value.equals (
					example)
			) {
				return true;
			}

		}

		return false;

	}

	public static
	boolean stringInSafe (
			@NonNull String value,
			@NonNull String example0) {

		return (

			value.equals (
				example0)

		);

	}

	public static
	boolean stringInSafe (
			@NonNull String value,
			@NonNull String example0,
			@NonNull String example1) {

		return (

			value.equals (
				example0)

			|| value.equals (
				example1)

		);

	}

	public static
	boolean stringInSafe (
			@NonNull String value,
			@NonNull String example0,
			@NonNull String example1,
			@NonNull String example2) {

		return (

			value.equals (
				example0)

			|| value.equals (
				example1)

			|| value.equals (
				example2)

		);

	}

	public static
	boolean stringInSafe (
			@NonNull String value,
			@NonNull String example0,
			@NonNull String example1,
			@NonNull String example2,
			@NonNull String example3) {

		return (

			value.equals (
				example0)

			|| value.equals (
				example1)

			|| value.equals (
				example2)

			|| value.equals (
				example3)

		);

	}

	public static
	boolean stringInSafe (
			@NonNull String value,
			@NonNull String example0,
			@NonNull String example1,
			@NonNull String example2,
			@NonNull String example3,
			@NonNull String example4) {

		return (

			value.equals (
				example0)

			|| value.equals (
				example1)

			|| value.equals (
				example2)

			|| value.equals (
				example3)

			|| value.equals (
				example4)

		);

	}

	public static
	boolean stringNotInSafe (
			@NonNull String value,
			@NonNull String... examples) {

		for (
			String example
				: examples
		) {

			if (
				value.equals (
					example)
			) {
				return false;
			}

		}

		return true;

	}

	public static
	boolean stringNotInSafe (
			@NonNull String value,
			@NonNull String example0) {

		return (

			! value.equals (
				example0)

		);

	}

	public static
	boolean stringNotInSafe (
			@NonNull String value,
			@NonNull String example0,
			@NonNull String example1) {

		return (

			! value.equals (
				example0)

			&& ! value.equals (
				example1)

		);

	}

	public static
	boolean stringNotInSafe (
			@NonNull String value,
			@NonNull String example0,
			@NonNull String example1,
			@NonNull String example2) {

		return (

			! value.equals (
				example0)

			&& ! value.equals (
				example1)

			&& ! value.equals (
				example2)

		);

	}

	public static
	boolean stringNotInSafe (
			@NonNull String value,
			@NonNull String example0,
			@NonNull String example1,
			@NonNull String example2,
			@NonNull String example3) {

		return (

			! value.equals (
				example0)

			&& ! value.equals (
				example1)

			&& ! value.equals (
				example2)

			&& ! value.equals (
				example3)

		);

	}

	public static
	boolean stringNotInSafe (
			@NonNull String value,
			@NonNull String example0,
			@NonNull String example1,
			@NonNull String example2,
			@NonNull String example3,
			@NonNull String example4) {

		return (

			! value.equals (
				example0)

			&& ! value.equals (
				example1)

			&& ! value.equals (
				example2)

			&& ! value.equals (
				example3)

			&& ! value.equals (
				example4)

		);

	}

	public static
	String stringReplaceAllSimple (
			@NonNull String from,
			@NonNull String to,
			@NonNull String subject) {

		return subject.replaceAll (
			Pattern.quote (
				from),
			to);

	}

	public static
	String stringReplaceAllRegex (
			@NonNull String fromPattern,
			@NonNull String to,
			@NonNull String subject) {

		return subject.replaceAll (
			fromPattern,
			to);

	}

	public static
	boolean stringMatches (
			@NonNull Pattern pattern,
			@NonNull String string) {

		Matcher matcher =
			pattern.matcher (
				string);

		return matcher.matches ();

	}

	public static
	boolean stringDoesNotMatch (
			@NonNull Pattern pattern,
			@NonNull String string) {

		Matcher matcher =
			pattern.matcher (
				string);

		return ! matcher.matches ();

	}

	public static
	boolean stringContains (
			@NonNull CharSequence substring,
			@NonNull CharSequence string) {

		return string.toString ().contains (
			substring);

	}

	public static
	boolean stringDoesNotContain (
			@NonNull CharSequence substring,
			@NonNull CharSequence string) {

		return ! string.toString ().contains (
			substring);

	}

	public static
	CharSequence keyEqualsClassSimple (
			@NonNull CharSequence key,
			@NonNull Class <?> value) {

		return stringFormatLazy (
			"%s = %s",
			key,
			classNameSimple (
				value));

	}

	public static
	CharSequence keyEqualsClassFull (
			@NonNull CharSequence key,
			@NonNull Class <?> value) {

		return stringFormatLazy (
			"%s = %s",
			key,
			classNameFull (
				value));

	}

	public static
	CharSequence keyEqualsString (
			@NonNull CharSequence key,
			@NonNull CharSequence value) {

		return stringFormatLazy (
			"%s = \"%s\"",
			key,
			value);

	}

	public static
	CharSequence keyEqualsDecimalInteger (
			@NonNull CharSequence key,
			@NonNull Long value) {

		return stringFormatLazy (
			"%s = %s",
			key,
			integerToDecimalString (
				value));

	}

	public static
	CharSequence keyEqualsDecimalInteger (
			@NonNull CharSequence key,
			@NonNull Integer value) {

		return stringFormatLazy (
			"%s = %s",
			key,
			integerToDecimalStringLazy (
				value));

	}

	public static
	CharSequence keyEqualsEnum (
			@NonNull CharSequence key,
			@NonNull Enum <?> value) {

		return stringFormatLazy (
			"%s = %s",
			key,
			enumNameHyphensLazy (
				value));

	}

	public static
	CharSequence keyEqualsYesNo (
			@NonNull CharSequence key,
			@NonNull Boolean value) {

		return stringFormatLazy (
			"%s = %s",
			key,
			booleanToYesNo (
				value));

	}

	public static
	long stringLength (
			@NonNull CharSequence charSequence) {

		return charSequence.toString ().length ();

	}

	public static
	long stringLength (
			@NonNull String string) {

		return string.length ();

	}

	public static
	boolean stringStartsWith (
			@NonNull String prefix,
			@NonNull String input) {

		if (
			lessThan (
				stringLength (
					input),
				stringLength (
					prefix))
		) {
			return false;
		}

		return stringEqualSafe (
			prefix,
			substring (
				input,
				0,
				stringLength (
					prefix)));

	}

	public static
	boolean stringDoesNotStartWith (
			@NonNull String prefix,
			@NonNull String input) {

		return ! stringStartsWith (
			prefix,
			input);

	}

	public static
	boolean stringEndsWith (
			@NonNull String suffix,
			@NonNull String input) {

		if (
			lessThan (
				stringLength (
					input),
				stringLength (
					suffix))
		) {
			return false;
		}

		return stringEqualSafe (
			suffix,
			substring (
				input,
				minus (
					stringLength (
						input),
					stringLength (
						suffix)),
				stringLength (
					input)));

	}

	public static
	boolean stringDoesNotEndWith (
			@NonNull String suffix,
			@NonNull String input) {

		return ! stringEndsWith (
			suffix,
			input);

	}

	public static
	Optional <String> stringExtract (
			@NonNull String prefix,
			@NonNull String suffix,
			@NonNull String input) {

		if (
			lessThan (
				stringLength (
					input),
				sum (
					stringLength (
						prefix),
					stringLength (
						suffix)))
		) {
			return optionalAbsent ();
		}

		if (
			stringDoesNotStartWith (
				prefix,
				input)
		) {
			return optionalAbsent ();
		}

		if (
			stringDoesNotEndWith (
				suffix,
				input)
		) {
			return optionalAbsent ();
		}

		return optionalOf (
			substring (
				input,
				stringLength (
					prefix),
				minus (
					stringLength (
						input),
					stringLength (
						suffix))));

	}

	public static
	String stringExtractRequired (
			@NonNull String prefix,
			@NonNull String suffix,
			@NonNull String input) {

		return optionalGetRequired (
			stringExtract (
				prefix,
				suffix,
				input));

	}

}
