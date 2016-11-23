package wbs.sms.smpp.daemon;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringFormat;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public
class SmppOutputStream
		extends FilterOutputStream {

	public
	SmppOutputStream (
			OutputStream out) {

		super (
			out);

	}

	public
	void writeCOctetString (
			String string,
			int maxLen)
		throws IOException {

		try {

			if (string == null)
				string = "";

			if (string.length() + 1 > maxLen)
				throw new IllegalArgumentException ();

			out.write (
				string.getBytes ("us-ascii"));

			out.write (
				0);

		} catch (UnsupportedEncodingException exception) {

			throw new IOException ();

		}

	}

	public
	void writeInteger (
			Integer value,
			int length)
		throws IOException {

		// handle nulls

		int newValue =
			value != null
				? value
				: 0;

		// nudge up to the left

		switch (length) {

		case 1:

			if (newValue > 0x000000ff) {

				throw new IllegalArgumentException (
					stringFormat (
						"Integer too big for length %s: %s",
						integerToDecimalString (
							length),
						integerToDecimalString (
							value)));

			}

			newValue <<= 24;

			break;

		case 2:

			if (newValue > 0x0000ffff) {

				throw new IllegalArgumentException (
					stringFormat (
						"Integer too big for length %s: %s",
						integerToDecimalString (
							length),
						integerToDecimalString (
							value)));

			}

			newValue <<= 16;

			break;

		case 4:

			break;

		default:

			if (length < 1 || length > 4) {

				throw new IllegalArgumentException (
					stringFormat (
						"Invalid integer size: %s",
						integerToDecimalString (
							length)));

			}

		}

		// write bytes

		for (; length > 0; length--) {

			out.write (
				(newValue & 0xff000000) >> 0x18);

			newValue <<= 8;

		}

	}

}
