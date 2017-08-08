package wbs.web.utils;

import java.util.Arrays;

import lombok.NonNull;

import wbs.utils.string.FormatWriter;

public
class HtmlScriptUtils {

	public static
	void htmlScriptBlockOpen (
			@NonNull FormatWriter formatWriter) {

		formatWriter.writeLineFormat (
			"<script type=\"text/javascript\">");

		formatWriter.increaseIndent ();

	}

	public static
	void htmlScriptBlockClose (
			@NonNull FormatWriter formatWriter) {

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"</script>");

	}

	public static
	void htmlScriptBlockWrite (
			@NonNull FormatWriter formatWriter,
			@NonNull String ... content) {

		htmlScriptBlockOpen (
			formatWriter);

		Arrays.asList (content).forEach (
			formatWriter::writeLineFormat);

		htmlScriptBlockClose (
			formatWriter);

	}

}
