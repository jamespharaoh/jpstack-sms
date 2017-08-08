package wbs.utils.io;

import static wbs.utils.etc.Misc.doNothing;

import java.io.IOException;
import java.io.OutputStream;

import lombok.NonNull;

public
class BorrowedOutputStream
	extends OutputStream {

	// state

	private final
	OutputStream delegate;

	// constructors

	public
	BorrowedOutputStream (
			@NonNull OutputStream delegate) {

		this.delegate =
			delegate;

	}

	@Override
	public
	void write (
			int oneByte)
		throws IOException {

		delegate.write (
			oneByte);

	}

	@Override
	public
	void write (
			byte[] bytes,
			int offset,
			int length)
		throws IOException {

		delegate.write (
			bytes,
			offset,
			length);

	}

	@Override
	public
	void close () {

		doNothing ();

	}

}
