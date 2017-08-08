package wbs.utils.io;

import java.io.IOException;
import java.io.Writer;

import lombok.NonNull;

public
class SafeWriter
	extends Writer {

	private final
	Writer target;

	public
	SafeWriter (
			@NonNull Writer target) {

		this.target =
			target;

	}

	@Override
	public
	void close () {

		try {

			target.close ();

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		}

	}

	@Override
	public
	void write (
			char[] cbuf,
			int off,
			int len)
		throws IOException {

		try {

			target.write (
				cbuf,
				off,
				len);

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		}

	}

	@Override
	public
	void flush ()
		throws IOException {

		try {

			target.flush ();

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		}

	}

}
