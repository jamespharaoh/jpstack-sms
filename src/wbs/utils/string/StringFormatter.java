package wbs.utils.string;

import static wbs.web.utils.HtmlUtils.htmlEncode;

import java.util.Iterator;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;

import lombok.NonNull;

import wbs.web.utils.HtmlUtils;

public
class StringFormatter {

	private final
	Map <Character, Conversion> conversions;

	public
	StringFormatter (
			@NonNull Map <Character, Conversion> newConversions) {

		conversions =
			newConversions;

	}

	public
	String format (
			@NonNull Iterable <String> arguments) {

		StringBuilder stringBuilder =
			new StringBuilder ();

		Iterator <String> iterator =
			arguments.iterator ();

		while (iterator.hasNext ()) {

			String format =
				iterator.next ();

			int numPercents =
				numPercents (
					format);

			stringBuilder.append (
				formatReal (
					format,
					() -> Iterators.limit (
						iterator,
						numPercents)));

		}

		return stringBuilder.toString ();

	}

	public
	String formatReal (
			@NonNull String format,
			@NonNull Iterable <String> argumenta) {

		StringBuilder stringBuilder =
			new StringBuilder ();

		int searchPosition = 0;

		Iterator<?> iterator =
			argumenta.iterator ();

		for (;;) {

			// find the next %

			int percentPosition =
				format.indexOf (
					'%',
					searchPosition);

			if (percentPosition < 0) {

				stringBuilder.append (
					format,
					searchPosition,
					format.length ());

				return stringBuilder.toString ();

			}

			// append the text leading up to the % bit

			stringBuilder.append (
				format,
				searchPosition,
				percentPosition);

			// get the next character

			if (percentPosition + 2 > format.length ()) {

				throw new RuntimeException (
					"Invalid format string - single % at end");

			}

			char formatCharacter =
				format.charAt (
					percentPosition + 1);

			// a double % inserts a single %

			if (formatCharacter == '%') {

				stringBuilder.append (
					'%');

				searchPosition =
					percentPosition + 2;

				continue;

			}

			// lookup the conversion

			Conversion conversion =
				conversions.get (
					formatCharacter);

			if (conversion == null) {

				throw new RuntimeException (
					"Invalid format char: " + formatCharacter);

			}

			// append the converted string

			stringBuilder.append (
				conversion.convert (
					iterator.next ()));

			searchPosition =
				percentPosition + 2;

		}

	}

	public static
	int numPercents (
			@NonNull String format) {

		int position = 0;
		int count = 0;

		for (;;) {

			position =
				format.indexOf (
					'%',
					position);

			if (position < 0)
				return count;

			if (position + 2 > format.length ()) {

				throw new RuntimeException (
					"Lone % at end of format string");

			}

			if (format.charAt (position + 1) != '%') {

				count ++;

			}

			position += 2;

		}

	}

	static
	interface Conversion {

		String convert (
			Object arg);

	}

	public static
	class StringConversion
		implements Conversion {

		@Override
		public
		String convert (
				@NonNull Object argument) {

			return argument.toString ();

		}

	}

	public static
	class HtmlConversion
		implements Conversion {

		@Override
		public
		String convert (
				@NonNull Object argument) {

			return htmlEncode (
				argument.toString ());

		}

	}

	public static
	class JavaScriptConversion
		implements Conversion {

		@Override
		public
		String convert (
				@NonNull Object argument) {

			return HtmlUtils.javascriptStringEscape (
				argument.toString ());

		}

	}

	public static
	class UrlConversion
		implements Conversion {

		@Override
		public
		String convert (
				@NonNull Object argument) {

			return HtmlUtils.urlQueryParameterEncode (
				argument.toString ());

		}

	}

	public static
	class DecimalConversion
		implements Conversion {

		@Override
		public
		String convert (
				@NonNull Object argument) {

			Number number =
				(Number)
				argument;

			return HtmlUtils.htmlEncode (
				number.toString ());

		}

	}

	private final static
	Map<Character,Conversion> standardConversions =
		ImmutableMap.<Character,Conversion>builder ()

		.put ('d', new DecimalConversion ())
		.put ('s', new StringConversion ())
		.put ('h', new HtmlConversion ())
		.put ('j', new JavaScriptConversion ())
		.put ('u', new UrlConversion ())

		.build ();

	public final static
	StringFormatter standardStringFormatter =
		new StringFormatter (
			standardConversions);

	public static
	String standard (
			@NonNull Iterable <String> arguments) {

		return standardStringFormatter.format (
			arguments);

	}

}
