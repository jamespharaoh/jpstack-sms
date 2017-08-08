package wbs.utils.string;

import static wbs.utils.string.StringUtils.stringFormatArray;
import static wbs.utils.string.StringUtils.stringIsNotEmpty;

import lombok.NonNull;

public
interface FormatWriterMethods {

	// core methods

	void writeString (
			LazyString string);

	String indentString ();

	FormatWriter indentString (
			String indentString);

	long indentSize ();

	FormatWriter indentSize (
			long indentSize);

	// default methods

	default
	void writeCharacter (
			int character) {

		writeString (
			LazyString.singleton (
				() -> new String (
					Character.toChars (
						character))));

	}

	default
	void writeString (
			@NonNull String string) {

		writeString (
			LazyString.singleton (
				string));

	}

	default
	void writeString (
			@NonNull CharSequence charSequence) {

		writeString (
			LazyString.singleton (
				charSequence));

	}

	default
	void writeFormat (
			CharSequence ... arguments) {

		writeString (
			stringFormatArray (
				arguments));

	}

	default
	void writeFormatArray (
			CharSequence[] arguments) {

		writeString (
			stringFormatArray (
				arguments));

	}

	default
	void writeLineFormat (
			@NonNull CharSequence ... arguments) {

		String lineContent =
			stringFormatArray (
				arguments);

		if (
			stringIsNotEmpty (
				lineContent)
		) {

			writeIndent ();

			writeString (
				lineContent);

			writeNewline ();

		}

	}

	default
	void writeLineFormatIncreaseIndent (
			@NonNull CharSequence ... arguments) {

		writeIndent ();

		writeString (
			stringFormatArray (
				arguments));

		writeNewline ();

		increaseIndent ();

	}

	default
	void writeLineFormatDecreaseIndent (
			@NonNull CharSequence ... arguments) {

		decreaseIndent ();

		writeIndent ();

		writeString (
			stringFormatArray (
				arguments));

		writeNewline ();

	}

	default
	void writeLineFormatDecreaseIncreaseIndent (
			@NonNull CharSequence ... arguments) {

		decreaseIndent ();

		writeIndent ();

		writeString (
			stringFormatArray (
				arguments));

		writeNewline ();

		increaseIndent ();

	}

	default
	void writeLineFormatArray (
			CharSequence[] arguments) {

		writeIndent ();

		writeString (
			stringFormatArray (
				arguments));

		writeNewline ();

	}

	default
	void writeNewline () {

		writeCharacter (
			'\n');

	}

	default
	void writeIndent () {

		for (
			long counter = 0l;
			counter < indentSize ();
			counter ++
		) {

			writeString (
				indentString ());

		}

	}

	default
	void writeNewlineAndIndent () {

		writeNewline ();

		writeIndent ();

	}

	default
	void increaseIndent () {

		indentSize (
			indentSize () + 1);

	}

	default
	void decreaseIndent () {

		indentSize (
			indentSize () - 1);

	}

}
