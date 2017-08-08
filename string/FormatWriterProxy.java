package wbs.utils.string;

import lombok.NonNull;

public
interface FormatWriterProxy <
	Implementation extends FormatWriterProxy <Implementation>
>
	extends FormatWriter {

	FormatWriter targetFormatWriter ();
	Implementation thisFormatWriter ();

	@Override
	default
	void writeString (
			@NonNull CharSequence string) {

		targetFormatWriter ().writeString (
			string);

	}

	@Override
	default
	void writeCharacter (
			int character) {

		targetFormatWriter ().writeCharacter (
			character);

	}

	@Override
	default
	String indentString () {

		return targetFormatWriter ().indentString ();

	}

	@Override
	default
	Implementation indentString (
			@NonNull String indentString) {

		targetFormatWriter ().indentString (
			indentString);

		return thisFormatWriter ();

	}

	@Override
	default
	long indentSize () {

		return targetFormatWriter ().indentSize ();

	}

	@Override
	default
	FormatWriter indentSize (
			long indentSize) {

		targetFormatWriter ().indentSize (
			indentSize);

		return thisFormatWriter ();

	}

}
