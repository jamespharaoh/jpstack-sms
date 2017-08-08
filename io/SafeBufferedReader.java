package wbs.utils.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import lombok.NonNull;

public
class SafeBufferedReader
	extends BufferedReader {

	public
	SafeBufferedReader (
			@NonNull Reader source) {

		super (
			source);

	}

	@Override
	public
	void close () {

		try {

			super.close ();

		} catch (IOException ioException) {

			throw new RuntimeIoException (
				ioException);

		}

	}

}
