package wbs.sms.gsm;

import static wbs.framework.utils.etc.StringUtils.joinWithPipe;

import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.NonNull;

/**
 * Class containing static utility functions for dealing with GSM characters and
 * so on.
 */
public
class GsmUtils {

	/**
	 * Private constructor never called.
	 */
	private
	GsmUtils () {
		throw new RuntimeException ();
	}

	/**
	 * Count the number of 7-bit gsm bytes required to store a string.
	 *
	 * @return the length
	 */
	public static
	long gsmStringLength (
			@NonNull String string) {

		if (! gsmStringIsValid (string)) {

			throw new InvalidGsmCharsException (
				"Provided string contains invalid GSM characters");

		}

		Matcher matcher =
			gsmDoubleCharsPattern.matcher (string);

		return matcher.replaceAll ("12").length ();

	}

	public static
	long gsmCountMessageParts (
			@NonNull Long gsmLength) {

		if (gsmLength <= 0) {

			throw new IllegalArgumentException (
				"Length must be a positive integer");

		} else if (gsmLength <= 160) {

			return 1;

		} else {

			return (gsmLength + 152) / 153;

		}

	}

	public static
	long gsmCountMessageParts (
			@NonNull String string) {

		return gsmCountMessageParts (
			gsmStringLength (
				string));

	}

	/**
	 * Check if a string consists solely of valid gsm characters.
	 *
	 * @param string
	 *            the string
	 * @return true if s consists of gsm characters only
	 */
	public static
	boolean gsmStringIsValid (
			@NonNull String string) {

		return gsmCharsPattern
			.matcher (string)
			.matches ();

	}

	public static
	boolean gsmStringIsNotValid (
			@NonNull String string) {

		return ! gsmStringIsValid (
			string);

	}

	/**
	 * Convert all versions of alphabetic latin characters in the string to
	 * their unaccented, lowercase form. Only works for gsm characters. This is
	 * typically used for keyword matching.
	 *
	 * @param string
	 *     The string to convert
	 *
	 * @return
	 *     The resulting string
	 *
	 * @throws InvalidGsmCharsException
	 *     If the string contains non-gsm characters
	 */
	public static
	String gsmStringSimplify (
			@NonNull String string) {

		// check for invalid characters

		if (
			gsmStringIsNotValid (
				string)
		) {
			throw new InvalidGsmCharsException ();
		}

		// convert the string

		StringBuilder stringBuilder =
			new StringBuilder ();

		for (
			int position = 0;
			position < string.length ();
			position ++
		) {

			char charValue =
				string.charAt (position);

			switch (charValue) {

			case 'A':
			case '\u00c4':
			case '\u00c5':
			case '\u00e4':
			case '\u00e5':
			case '\u00e0':

				stringBuilder.append (
					'a');

				break;

			case 'B':

				stringBuilder.append (
					'b');

				break;

			case 'C':
			case '\u00c7':

				stringBuilder.append (
					'c');

				break;

			case 'D':

				stringBuilder.append (
					'd');

				break;

			case 'E':
			case '\u00c9':
			case '\u00e8':
			case '\u00e9':

				stringBuilder.append (
					'e');

				break;

			case 'F':

				stringBuilder.append (
					'f');

				break;

			case 'G':

				stringBuilder.append (
					'g');

				break;

			case 'H':

				stringBuilder.append (
					'h');

				break;

			case 'I':
			case '\u00ec':

				stringBuilder.append (
					'i');

				break;

			case 'J':

				stringBuilder.append (
					'j');

				break;

			case 'K':

				stringBuilder.append (
					'k');

				break;

			case 'L':

				stringBuilder.append (
					'l');

				break;

			case 'M':

				stringBuilder.append (
					'm');

				break;

			case 'N':
			case '\u00d1':
			case '\u00f1':

				stringBuilder.append (
					'n');

				break;

			case 'O':
			case '\u00d6':
			case '\u00d8':
			case '\u00f2':
			case '\u00f6':
			case '\u00f8':

				stringBuilder.append (
					'o');

				break;

			case 'P':

				stringBuilder.append (
					'p');

				break;

			case 'Q':

				stringBuilder.append (
					'q');

				break;

			case 'R':

				stringBuilder.append (
					'r');

				break;

			case 'S':

				stringBuilder.append (
					's');

				break;

			case 'T':

				stringBuilder.append (
					't');

				break;

			case 'U':
			case '\u00dc':
			case '\u00fc':
			case '\u00f9':

				stringBuilder.append (
					'u');

				break;

			case 'V':

				stringBuilder.append (
					'v');

				break;

			case 'W':

				stringBuilder.append (
					'w');

				break;

			case 'X':

				stringBuilder.append (
					'x');

				break;

			case 'Y':

				stringBuilder.append (
					'y');

				break;

			case 'Z':

				stringBuilder.append (
					'z');

				break;

			case '\u00c6':
			case '\u00e6':

				stringBuilder.append (
					"ae");

				break;

			default:

				stringBuilder.append (
					charValue);

				break;

			}

		}

		return stringBuilder.toString ();

	}

	/**
	 * Unpacks 7 bit bytes from an 8 bit stream, as defined by the GSM standard.
	 *
	 * @param byteBuffer
	 *            The buffer to get the 8 bit data from.
	 * @param length
	 *            The number of 7bit bytes to unpack.
	 * @return An array of bytes representing the 7bit data.
	 */
	public static
	byte[] gsmFrom7BitPacked (
			@NonNull ByteBuffer byteBuffer,
			int length) {

		byte[] targetByteArray =
			new byte [length];

		int byteValue = 0;

		for (
			int index = 0;
			index < length;
			index ++
		) {

			switch (index & 0x07) {

			case 0x00:

				byteValue =
					byteBuffer.get() & 0xff;

				targetByteArray [index] =
					(byte)
					(byteValue & 0x7f); // 0111 1111

				break;

			case 0x01:

				targetByteArray [index] =
					(byte)
					((byteValue & 0x80) >> 7); // 1000 0000

				byteValue =
					byteBuffer.get () & 0xff;

				targetByteArray [index] |=
					(byteValue & 0x3f) << 1; // 0011 1111

				break;

			case 0x02:

				targetByteArray [index] =
					(byte)
					((byteValue & 0xc0) >> 6); // 1100 0000

				byteValue =
					byteBuffer.get () & 0xff;

				targetByteArray [index] |=
					(byteValue & 0x1f) << 2; // 0001 1111

				break;

			case 0x03:

				targetByteArray [index] =
					(byte)
					((byteValue & 0xe0) >> 5); // 1110 0000

				byteValue =
					byteBuffer.get() & 0xff;

				targetByteArray[index] |=
					(byteValue & 0x0f) << 3; // 0000 1111

				break;

			case 0x04:

				targetByteArray [index] = (byte)
					((byteValue & 0xf0) >> 4); // 1111 0000

				byteValue =
					byteBuffer.get () & 0xff;

				targetByteArray [index] |=
					(byteValue & 0x07) << 4; // 0000 0111

				break;

			case 0x05:

				targetByteArray [index] =
					(byte)
					((byteValue & 0xf8) >> 3); // 1111 1000

				byteValue =
					byteBuffer.get () & 0xff;

				targetByteArray [index] |=
					(byteValue & 0x03) << 5; // 0000 0011

				break;

			case 0x06:

				targetByteArray [index] =
					(byte)
					((byteValue & 0xfc) >> 2); // 1111 1100

				byteValue =
					byteBuffer.get () & 0xff;

				targetByteArray [index] |=
					(byteValue & 0x01) << 6; // 0000 0001

				break;

			case 0x07:

				targetByteArray [index] =
					(byte)
					((byteValue & 0xfe) >> 1); // 1111 1110

				break;

			}

		}

		return targetByteArray;

	}

	public static
	String decode (
			byte[] gsmBytes) {

		StringBuilder stringBuilder =
			new StringBuilder ();

		ByteBuffer byteBuffer =
			ByteBuffer.wrap (gsmBytes);

		while (byteBuffer.hasRemaining ()) {

			int byteValue =
				byteBuffer.get () & 0xff;

			if (byteValue == 0x1b) {

				byteValue =
					byteBuffer.get () & 0xff;

				char charValue =
					gsmChars2 [byteValue];

				if (charValue == 0)
					throw new IllegalArgumentException ();

				stringBuilder.append (
					charValue);

			} else {

				stringBuilder.append (
					gsmChars1 [byteValue]);

			}

		}

		return stringBuilder.toString ();

	}

	/**
	 * Pattern which matches any number of gsm characters.
	 */
	public static final
	Pattern gsmCharsPattern =
		Pattern.compile (
			"[" +
			"@\u00a3$\u00a5\u00e8\u00e9\u00f9\u00ec\u00f2\u00e7\n\u00d8\u00f8\r\u00c5\u00e5" +
			"\u0394_\u03a6\u0393\u039b\u03a9\u03a0\u03a8\u03a3\u0398\u039e\u00c6\u00e6\u00df\u00c9" +
			" !\"#\u00a4%&'()*+,\\-\\./" +
			"0123456789:;<=>?" +
			"\u00a1ABCDEFGHIJKLMNO" +
			"PQRSTUVWXYZ\u00c4\u00d6\u00d1\u00dc\u00a7" +
			"\u00bfabcdefghijklmno" +
			"pqrstuvwxyz\u00e4\u00f6\u00f1\u00fc\u00e0" +
			"\u000c" + // form feed
			"^{|}" +
			"\\\\" + // backslash
			"\\[" + // left square brace
			"~" + // tilde
			"\\]" + // right square brace
			"\u20ac" + // euro currency symbol
			"]*");

	/**
	 * Pattern matching characters which take two bytes in GSM.
	 */
	public static final
	Pattern gsmDoubleCharsPattern =
		Pattern.compile (
			joinWithPipe (
				"\\u000c", // form feed
				"\\[", // left square brace [
				"\\\\", // backslash \
				"\\]", // right square brace ]
				"\\^", // hat ^
				"\u20ac", // euro currency symbol
				"[{|}~]")); // easy characters { | } ~

	/**
	 * Character map for GSM single-byte characters
	 */
	public static final
	char [] gsmChars1 = {
		'@',      '\u00a3', '$',      '\u00a5',
		'\u00e8', '\u00e9', '\u00f9', '\u00ec',
		'\u00f2', '\u00c7', '\n',     '\u00d8',
		'\u00f8', '\r',     '\u00c5', '\u00e5',
		'\u0394', '_',      '\u03a6', '\u0393',
		'\u039b', '\u03a9', '\u03a0', '\u03a8',
		'\u03a3', '\u0398', '\u039e', ' ',
		'\u00c6', '\u00e6', '\u00df', '\u00c9',
		' ',      '!',      '"',      '#',
		'\u00a4', '%',      '&',      '\'',
		'(',      ')',      '*',      '+',
		',',      '-',      '.',      '/',
		'0',      '1',      '2',      '3',
		'4',      '5',      '6',      '7',
		'8',      '9',      ':',      ';',
		'<',      '=',      '>',      '?',
		'\u00a1', 'A',      'B',      'C',
		'D',      'E',      'F',      'G',
		'H',      'I',      'J',      'K',
		'L',      'M',      'N',      'O',
		'P',      'Q',      'R',      'S',
		'T',      'U',      'V',      'W',
		'X',      'Y',      'Z',      '\u00c4',
		'\u00d6', '\u00d1', '\u00dc', '\u00a7',
		'\u00bf', 'a',      'b',      'c',
		'd',      'e',      'f',      'g',
		'h',      'i',      'j',      'k',
		'l',      'm',      'n',      'o',
		'p',      'q',      'r',      's',
		't',      'u',      'v',      'w',
		'x',      'y',      'z',      '\u00e4',
		'\u00f6', '\u00f1', '\u00fc', '\u00e0'
	};

	/**
	 * Character map for GSM double-byte (escaped) characters
	 */
	public static final
	char[] gsmChars2 = {
		0,        0,        0,        0,
		0,        0,        0,        0,
		0,        0,        0,        0,
		0,        0,        0,        0,
		0,        0,        0,        0,
		'^',      0,        0,        0,
		0,        0,        0,        0,
		0,        0,        0,        0,
		0,        0,        0,        0,
		0,        0,        0,        0,
		'{',      '}',      0,        0,
		0,        0,        0,        '\\',
		0,        0,        0,        0,
		0,        0,        0,        0,
		0,        0,        0,        0,
		'[',      '~',      ']',      0,
		0,        0,        0,        0,
		0,        0,        0,        0,
		0,        0,        0,        0,
		0,        0,        0,        0,
		0,        0,        0,        0,
		0,        0,        0,        0,
		0,        0,        0,        0,
		0,        0,        0,        0,
		0,        0,        0,        0,
		0,        '\u20ac', 0,        0,
		0,        0,        0,        0,
		0,        0,        0,        0,
		0,        0,        0,        0,
		0,        0,        0,        0,
		0,        0,        0,        0,
		0,        0,        0,        0
	};

}
