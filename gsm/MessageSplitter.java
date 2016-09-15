package wbs.sms.gsm;

import static wbs.utils.etc.Misc.stringTrim;
import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;
import static wbs.utils.string.StringUtils.substring;
import static wbs.utils.string.StringUtils.substringFrom;
import static wbs.utils.string.StringUtils.substringTo;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public
class MessageSplitter {

	public final static
	class Templates {

		private final
		String single, first, middle, last;

		public
		Templates (
				String newSingle,
				String newFirst,
				String newMiddle,
				String newLast) {

			if (! GsmUtils.gsmStringIsValid (newSingle)) {

				throw new IllegalArgumentException (
					"Non-GSM characters in single template: " + newSingle);

			}

			if (! GsmUtils.gsmStringIsValid (newFirst)) {

				throw new IllegalArgumentException (
					"Non-GSM characters in first template: " + newFirst);

			}

			if (! GsmUtils.gsmStringIsValid (newMiddle)) {

				throw new IllegalArgumentException (
					"Non-GSM characters in middle template: " + newMiddle);

			}

			if (! GsmUtils.gsmStringIsValid (newLast)) {

				throw new IllegalArgumentException(
					"Non-GSM characters in last template: " + newLast);

			}

			single = newSingle;
			first = newFirst;
			middle = newMiddle;
			last = newLast;

		}

		public
		String getSingle () {
			return single;
		}

		public
		String getFirst () {
			return first;
		}

		public
		String getMiddle () {
			return middle;
		}

		public
		String getLast () {
			return last;
		}

	}

	private
	MessageSplitter () {
		// never instantiated
	}

	/**
	 * Splits a single GSM message off the start of the supplied mesage, into
	 * the supplied template. Called repeatedly by the split function below.
	 */
	private
	static String[] splitOne (
			String message,
			String template,
			int page,
			int pages) {

		message = message.trim ();

		// put page numbers in

		template =
			template

			.replaceFirst (
				"\\{page}",
				Matcher.quoteReplacement (
					Integer.toString (
						page)))

			.replaceFirst (
				"\\{pages}",
				Matcher.quoteReplacement (
					Integer.toString (
						pages)));

		long spareLength =
			160 - GsmUtils.gsmStringLength (
				template.replaceFirst (
					"\\{message}",
					""));

		if (spareLength < 2) {

			throw new IllegalArgumentException (
				"No spare space in template");

		}

		// if the message will fit as is then that's cool

		if (GsmUtils.gsmStringLength (message) <= spareLength) {

			return new String [] {

				template.replaceFirst (
					"\\{message}",
					Matcher.quoteReplacement (
						message)),

				null

			};
		}

		// find how much we can fit in

		long maxSplit =
			spareLength;

		while (
			GsmUtils.gsmStringLength (
				message.substring (
					0,
					toJavaIntegerRequired (
						maxSplit))
			) > spareLength
		) {

			maxSplit --;

		}

		// now go backwards to find a good place to split

		long minSplit =
			(maxSplit + 2) * 2 / 3;

		for (
			long position = maxSplit;
			position >= minSplit;
			position --
		) {

			char character =
				message.charAt (
					toJavaIntegerRequired (
						position));

			if (character == ' ') {

				String part1 =
					stringTrim (
						substring (
							message,
							0,
							position));

				String part2 =
					stringTrim (
						substringFrom (
							message,
							position));

				return new String [] {

					template.replaceFirst (
						"\\{message}",
						Matcher.quoteReplacement (
							part1)),

					part2

				};

			}

		}

		// that didn't work, just split it anywhere

		String part1 =
			stringTrim (
				substringTo (
					message,
					maxSplit));

		String part2 =
			stringTrim (
				substringFrom (
					message,
					maxSplit));

		return new String [] {

			template.replaceFirst (
				"\\{message}",
				Matcher.quoteReplacement (
					part1)),

			part2

		};

	}

	/**
	 * Splits a message into parts which will fit into a single SMS, according
	 * to templtes for first/middle/last pages. Put the text {page}, {pages} and
	 * {message} in each template to be replaced by the appropriate information.
	 *
	 * Works on the assumption that first template has space less than or equal
	 * to single template, and that middle template has space less than or equal
	 * to last template, or you might get an incorrect page count. I can't think
	 * of a particularly clean solution in these cases anyway.
	 *
	 * @param message
	 *            the message to be split.
	 * @param templates
	 *            the message templates to use.
	 * @return a list of messages.
	 * @throws IllegalArgumentException
	 *             if a needed template has less than 2 gsm bytes of space, or
	 *             if non-GSM characters are found in the message or any used
	 *             template.
	 */
	public static
	List<String> split (
			String message,
			Templates templates) {

		if (! GsmUtils.gsmStringIsValid (
				message)) {

			throw new IllegalArgumentException (
				"Non-GSM characters in message: " + message);

		}

		// try template0 first, with one page

		String singleMessage =
			templates.single

			.replaceFirst (
				"\\{page}",
				"1")

			.replaceFirst (
				"\\{pages}",
				"1")

			.replaceFirst (
				"\\{message}",
				Matcher.quoteReplacement (
					message.trim ()));

		if (GsmUtils.gsmStringLength (singleMessage) <= 160) {

			List<String> result =
				new ArrayList<String> ();

			result.add (
				singleMessage);

			return result;

		}

		for (int pages = 1; true; pages++) {
			List<String> result = new ArrayList<String>();
			String[] parts = new String[] { null, message };
			for (int page = 1; page <= pages; page++) {

				String template;
				if (page == 1)
					template = templates.first;
				else if (page == pages)
					template = templates.last;
				else
					template = templates.middle;

				parts = splitOne(parts[1], template, page, pages);
				result.add(parts[0]);

				if (parts[1] == null)
					return result;
			}
		}
	}

}
