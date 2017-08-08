package wbs.utils.string;

import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.function.Consumer;

import com.google.common.base.Function;

import lombok.NonNull;

public
class FormatWriterUtils {

	public static
	String formatWriterConsumerToString (
			@NonNull String indent,
			@NonNull Consumer <FormatWriter> consumer) {

		try (

			LazyFormatWriter formatWriter =
				new LazyFormatWriter ();


		) {

			formatWriter.indentString (
				"  ");

			consumer.accept (
				formatWriter);

			return formatWriter.toString ();

		}

	}

	public static <Type>
	void writeLines (
			@NonNull FormatWriter formatWriter,
			@NonNull Function <String, String> onlyLineFunction,
			@NonNull Function <String, String> firstLineFunction,
			@NonNull Function <String, String> middleLineFunction,
			@NonNull Function <String, String> lastLineFunction,
			@NonNull Iterable <String> lines) {

		Boolean firstLine = true;
		String previousLine = null;

		for (
			String line
				: lines
		) {

			if (
				isNotNull (
					previousLine)
			) {

				if (firstLine) {

					formatWriter.writeLineFormat (
						"%s",
						firstLineFunction.apply (
							previousLine));

				} else {

					formatWriter.writeLineFormat (
						"%s",
						middleLineFunction.apply (
							previousLine));

				}

				firstLine = false;

			}

			previousLine = line;

		}

		if (firstLine) {

			formatWriter.writeLineFormat (
				"%s",
				onlyLineFunction.apply (
					previousLine));

		} else {

			formatWriter.writeLineFormat (
				"%s",
				lastLineFunction.apply (
					previousLine));

		}

	}

	public static <Type>
	void writeLines (
			@NonNull FormatWriter formatWriter,
			@NonNull String onlyLineTemplate,
			@NonNull String firstLineTemplate,
			@NonNull String middleLineTemplate,
			@NonNull String lastLineTemplate,
			@NonNull Iterable <String> lines) {

		writeLines (
			formatWriter,
			line ->
				stringFormat (
					onlyLineTemplate,
					line),
			line ->
				stringFormat (
					firstLineTemplate,
					line),
			line ->
				stringFormat (
					middleLineTemplate,
					line),
			line ->
				stringFormat (
					lastLineTemplate,
					line),
			lines);

	}

	public static <Type>
	void writeLinesWithCommaAlways (
			@NonNull FormatWriter formatWriter,
			@NonNull Iterable <String> lines) {

		writeLines (
			formatWriter,
			"%s,",
			"%s,",
			"%s,",
			"%s,",
			lines);

	}

	public static <Type>
	void writeLinesWithCommaExceptLastLine (
			@NonNull FormatWriter formatWriter,
			@NonNull Iterable <String> lines) {

		writeLines (
			formatWriter,
			"%s",
			"%s,",
			"%s,",
			"%s",
			lines);

	}

}
