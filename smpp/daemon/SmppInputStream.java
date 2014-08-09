package wbs.sms.smpp.daemon;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public
class SmppInputStream
	extends FilterInputStream {

	public
	SmppInputStream (
			InputStream inputStream) {

		super (
			inputStream);

	}

	public
	String readCOctetString (
			int maxLen)
		throws IOException {

		if (maxLen < 1) {

			throw new IllegalArgumentException (
				"Cannot read string of length " + maxLen);

		}

		char[] buf =
			new char [maxLen - 1];

		for (
			int chars = 0;
			chars < maxLen;
			chars ++
		) {

			int val =
				read ();

			if (val < 0)
				throw new EOFException();

			if (val == 0)
				return new String(buf, 0, chars);

			if (val >= 0x80) {

				throw new IOException (
					"Invalid ascii code " + val);

			}

			buf [chars] =
				(char) val;

		}

		throw new IOException (
			"Unterminated C-Octet-String");

	}

	public
	int readInteger (
			int length)
		throws IOException {

		// check params

		if (
			length != 1
			&& length != 2
			&& length != 4
		) {

			throw new IllegalArgumentException (
				"Invalid int size: " + length);

		}

		// read bytes

		int ret = 0;

		for (; length > 0; length--) {

			ret <<= 8;

			int i = in.read();

			if (i < 0)
				throw new EOFException();

			ret |= i;

		}

		// return

		return ret;

	}

}