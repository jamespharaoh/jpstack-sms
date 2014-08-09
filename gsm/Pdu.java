package wbs.sms.gsm;

import java.nio.ByteBuffer;
import java.util.regex.Pattern;

/**
 * Abstract base class for Pdu data structures. Also includes static methods for
 * decoding them.
 */
public abstract
class Pdu {

	protected
	Pdu () {
		// never instantiated
	}

	/**
	 * Decodes a byte[] into a Pdu structure. Currently only works for
	 * SMS-SUBMIT.
	 *
	 * @param byteBuffer
	 *            The raw PDU data.
	 * @return A Pdu representing this data.
	 * @throws PduDecodeException
	 *             if invalid or unsupported data is encountered.
	 */
	public static
	Pdu decode (
			ByteBuffer byteBuffer)
		throws PduDecodeException {

		int position =
			byteBuffer.position ();

		int mti =
			byteBuffer.get () & 0x03;

		byteBuffer.position (
			position);

		switch (mti) {

		case 0x0: // SMS-DELIVER

			return SmsDeliverPdu.decode (
				byteBuffer);

		default:

			throw new PduDecodeException (
				"Unsupported PDU type: " + mti);

		}

	}

	/**
	 * Pulls an SMSC header off the front of a PDU.
	 */
	public static
	void skipSmsc (
			ByteBuffer byteBuffer)
		throws PduDecodeException {

		int smsclen =
			byteBuffer.get () & 0x0ff;

		byteBuffer.position (
			byteBuffer.position () + smsclen);

	}

	private final static
	Pattern hexPduPattern =
		Pattern.compile (
			"([0123456789abcdefABCDEF]{2})*");

	/**
	 * Turns string containing a series of hex bytes (big nibble first) into a
	 * byte[].
	 *
	 * Example PDU in hex format:
	 *
	 * 0791449737019037240C9144876667052800005020615155430003C87408
	 */
	public static
	byte[] hexToByteArray (
			String hex) {

		if (! hexPduPattern.matcher (hex).matches ()) {

			throw new IllegalArgumentException (
				"Illegal hex");

		}

		byte[] bytes =
			new byte [hex.length () >> 1];

		for (
			int index = 0;
			index < bytes.length;
			index++
		) {

			int position =
				index << 1;

			bytes [index] = (byte) (
				(decodeHexNibble (hex.charAt (position)) << 4)
				| decodeHexNibble (hex.charAt (position + 1)));

		}

		return bytes;

	}

	public static
	int decodeHexNibble (
			char charValue) {

		switch (charValue) {

		case '0':
			return 0;

		case '1':
			return 1;

		case '2':
			return 2;

		case '3':
			return 3;

		case '4':
			return 4;

		case '5':
			return 5;

		case '6':
			return 6;

		case '7':
			return 7;

		case '8':
			return 8;

		case '9':
			return 9;

		case 'a':
		case 'A':
			return 10;

		case 'b':
		case 'B':
			return 11;

		case 'c':
		case 'C':
			return 12;

		case 'd':
		case 'D':
			return 13;

		case 'e':
		case 'E':
			return 14;

		case 'f':
		case 'F':
			return 15;

		default:
			throw new IllegalArgumentException ();

		}

	}

}
