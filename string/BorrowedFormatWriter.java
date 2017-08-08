package wbs.utils.string;

import static wbs.utils.etc.Misc.doNothing;

import lombok.NonNull;

public
class BorrowedFormatWriter
	implements FormatWriter {

	// state

	private final
	FormatWriter delegate;

	// constructors

	public
	BorrowedFormatWriter (
			@NonNull FormatWriter delegate) {

		this.delegate =
			delegate;

	}

	// implementation

	@Override
	public
	void close () {
		doNothing ();
	}

	@Override
	public
	long indentSize () {
		return delegate.indentSize ();
	}

	@Override
	public
	FormatWriter indentSize (
			long indentSize) {

		delegate.indentSize (
			indentSize);

		return this;

	}

	@Override
	public
	String indentString () {
		return delegate.indentString ();
	}

	@Override
	public
	FormatWriter indentString (
			@NonNull String indentString) {

		delegate.indentString (
			indentString);

		return this;

	}

	@Override
	public
	void writeString (
			@NonNull LazyString string) {

		delegate.writeString (
			string);

	}

}
