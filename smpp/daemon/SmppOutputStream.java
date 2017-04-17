package wbs.sms.smpp.daemon;

import static wbs.utils.etc.IoUtils.writeByte;
import static wbs.utils.etc.IoUtils.writeBytes;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringToBytes;

import java.io.FilterOutputStream;
import java.io.OutputStream;

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
			String originalString,
			int maxLen) {

		String string =
			ifNull (
				originalString,
				"");

		if (string.length () + 1 > maxLen) {
			throw new IllegalArgumentException ();
		}

		writeBytes (
			out,
			stringToBytes (
				string,
				"us-ascii"));

		writeByte (
			out,
			0);

	}

	public
	void writeInteger (
			Integer value,
			int originalLength) {

		// handle nulls

		int newValue =
			value != null
				? value
				: 0;

		// nudge up to the left

		switch (originalLength) {

		case 1:

			if (newValue > 0x000000ff) {

				throw new IllegalArgumentException (
					stringFormat (
						"Integer too big for length %s: %s",
						integerToDecimalString (
							originalLength),
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
							originalLength),
						integerToDecimalString (
							value)));

			}

			newValue <<= 16;

			break;

		case 4:

			break;

		default:

			if (originalLength < 1 || originalLength > 4) {

				throw new IllegalArgumentException (
					stringFormat (
						"Invalid integer size: %s",
						integerToDecimalString (
							originalLength)));

			}

		}

		// write bytes

		for (
			int length = originalLength;
			length > 0;
			length --
		) {

			writeByte (
				out,
				(newValue & 0xff000000) >> 0x18);

			newValue <<= 8;

		}

	}

}
