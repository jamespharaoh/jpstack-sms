package wbs.utils.string;

import java.util.function.Consumer;

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

}
