package wbs.utils.string;

import static wbs.utils.string.StringUtils.stringNotEqualSafe;

import java.io.File;
import java.io.IOException;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.io.FileUtils;

import wbs.utils.io.RuntimeIoException;

@Accessors (fluent = true)
public
class AtomicFileWriter
	implements FormatWriter {

	// properties

	@Getter @Setter
	String indentString = "\t";

	@Getter @Setter
	long indentSize = 0;

	// state

	File file;

	LazyFormatWriter lazyFormatWriter =
		new LazyFormatWriter ();

	// implementation

	public
	AtomicFileWriter (
			@NonNull String filename) {

		file =
			new File (
				filename);

	}

	@Override
	public
	void writeString (
			@NonNull LazyString lazyString) {

		lazyFormatWriter.writeString (
			lazyString);

	}

	public
	void commit () {

		String newContents =
			lazyFormatWriter.toString ();

		if (
			! contentHasChanged (
				newContents)
		) {
			return;
		}

		try {

			FileUtils.writeStringToFile (
				file,
				newContents);

		} catch (IOException exception) {

			throw new RuntimeIoException (
				exception);

		}

	}

	private
	boolean contentHasChanged (
			@NonNull String newContents) {

		if (! file.exists ()) {

			return true;
		}

		try {

			String oldContents =
				FileUtils.readFileToString (
					file);

			return stringNotEqualSafe (
				oldContents,
				newContents);

		} catch (IOException exception) {

			throw new RuntimeIoException (
				exception);

		}

	}

	@Override
	public
	void close () {

		lazyFormatWriter.close ();

	}

}
