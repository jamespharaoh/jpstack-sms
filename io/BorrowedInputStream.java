package wbs.utils.io;

import static wbs.utils.etc.Misc.doNothing;

import java.io.IOException;
import java.io.InputStream;

import lombok.NonNull;

public
class BorrowedInputStream
	extends InputStream {

	// state

	private final
	InputStream delegate;

	// constructors

	public
	BorrowedInputStream (
			@NonNull InputStream delegate) {

		this.delegate =
			delegate;

	}

	// implementation

	@Override
	public
	int read ()
			throws IOException {

		return delegate.read ();

	}

	@Override
	public
	int available ()
			throws IOException {

		return delegate.available ();

	}

	@Override
	public
	void close () {

		doNothing ();

	}

	@Override
	public synchronized
	void mark (
			int readLimit) {

		delegate.mark (
			readLimit);

	}

	@Override
	public
	boolean markSupported () {

		return delegate.markSupported ();

	}

	@Override
	public
	int read (
			@NonNull byte[] bytes)
		throws IOException {

		return delegate.read (
			bytes);

	}

	@Override
	public
	int read (
			byte[] bytes,
			int offset,
			int length)
		throws IOException {

		return delegate.read (
			bytes,
			offset,
			length);

	}

	@Override
	public synchronized
	void reset ()
			throws IOException {

		delegate.reset ();

	}

	@Override
	public
	long skip (
			long numBytes)
		throws IOException {

		return delegate.skip (
			numBytes);

	}

}
