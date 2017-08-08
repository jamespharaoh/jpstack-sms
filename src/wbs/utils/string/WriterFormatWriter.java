package wbs.utils.string;

import java.io.IOException;
import java.io.Writer;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.utils.io.RuntimeIoException;

@Accessors (fluent = true)
public
class WriterFormatWriter
	implements FormatWriter {

	// properties

	@Getter @Setter
	String indentString = "\t";

	@Getter @Setter
	long indentSize = 0l;

	// state

	Writer writer;

	// constructors

	public
	WriterFormatWriter (
			Writer writer) {

		this.writer = writer;

	}

	// implementation

	@Override
	public
	void writeString (
			@NonNull LazyString lazyString) {

		try {

			for (
				String part
					: lazyString.toParts ()
			) {

				writer.write (
					part);

			}

		} catch (IOException exception) {

			throw new RuntimeIoException (exception);

		}

	}

	@Override
	public
	void writeCharacter (
			int character) {

		try {

			writer.append (
				(char) character);

		} catch (IOException exception) {

			throw new RuntimeIoException (exception);

		}

	}

	@Override
	public
	void close () {

		try {

			writer.close ();

		} catch (IOException exception) {

			throw new RuntimeIoException (exception);

		}

	}

}
