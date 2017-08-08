package wbs.web.utils;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringFormatArray;
import static wbs.web.utils.HtmlAttributeUtils.htmlAttributesWrite;

import java.util.Arrays;
import java.util.function.Consumer;

import lombok.NonNull;

import wbs.utils.string.FormatWriter;

import wbs.web.utils.HtmlAttributeUtils.HtmlAttribute;
import wbs.web.utils.HtmlAttributeUtils.ToHtmlAttribute;

public
class HtmlTableUtils {

	public static
	void htmlTableHeaderRowWrite (
			@NonNull FormatWriter formatWriter,
			@NonNull String ... headerLabels) {

		htmlTableRowOpen (
			formatWriter);

		String currentLabel = null;
		long currentColumnSpan = 0;

		for (
			String headerLabel
				: headerLabels
		) {

			if (currentLabel == null) {

				// first column, just store it

				if (headerLabel == null) {
					throw new RuntimeException ();
				}

				currentLabel =
					headerLabel;

				currentColumnSpan = 1;

			} else if (headerLabel == null) {

				// null value continue previous column

				currentColumnSpan ++;

			} else {

				// new header, output pending

				if (currentColumnSpan > 1) {

					formatWriter.writeLineFormat (
						"<th colspan=\"%h\">%h</th>",
						integerToDecimalString (
							currentColumnSpan),
						currentLabel);

				} else {

					formatWriter.writeLineFormat (
						"<th>%h</th>",
						currentLabel);

				}

				// update current

				currentLabel =
					headerLabel;

				currentColumnSpan = 1;

			}

		}

		// output final header cell

		if (currentColumnSpan > 1) {

			formatWriter.writeLineFormat (
				"<th colspan=\"%h\">%h</th>",
				integerToDecimalString (
					currentColumnSpan),
				currentLabel);

		} else {

			formatWriter.writeLineFormat (
				"<th>%h</th>",
				currentLabel);

		}

		// close table

		htmlTableRowClose (
			formatWriter);

	}

	public static
	void htmlTableHeaderRowWriteHtml (
			@NonNull FormatWriter formatWriter,
			@NonNull String ... headerLabels) {

		htmlTableRowOpen (
			formatWriter);

		for (
			String headerLabel
				: headerLabels
		) {

			formatWriter.writeLineFormat (
				"<th>%s</th>",
				headerLabel);

		}

		htmlTableRowClose (
			formatWriter);

	}

	public static
	void htmlTableOpen (
			@NonNull FormatWriter formatWriter,
			@NonNull ToHtmlAttribute ... attributes) {

		formatWriter.writeIndent ();

		formatWriter.writeFormat (
			"<table");

		htmlAttributesWrite (
			formatWriter,
			attributes);

		formatWriter.writeFormat (
			">");

		formatWriter.writeNewline ();

		formatWriter.increaseIndent ();

	}

	public static
	void htmlTableOpenDetails (
			@NonNull FormatWriter formatWriter) {

		formatWriter.writeLineFormat (
			"<table class=\"details\">");

		formatWriter.increaseIndent ();

	}

	public static
	void htmlTableOpenLayout (
			@NonNull FormatWriter formatWriter) {

		formatWriter.writeLineFormat (
			"<table class=\"layout\">");

		formatWriter.increaseIndent ();

	}

	public static
	void htmlTableOpenList (
			@NonNull FormatWriter formatWriter,
			@NonNull ToHtmlAttribute ... attributes) {

		formatWriter.writeIndent ();

		formatWriter.writeFormat (
			"<table",
			" class=\"list\"");

		htmlAttributesWrite (
			formatWriter,
			attributes);

		formatWriter.writeFormat (
			">");

		formatWriter.writeNewline ();

		formatWriter.increaseIndent ();

	}

	public static
	void htmlTableClose (
			@NonNull FormatWriter formatWriter) {

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"</table>");

	}

	// table head

	public static
	void htmlTableHeadOpen (
			@NonNull FormatWriter formatWriter,
			@NonNull ToHtmlAttribute ... attributes) {

		formatWriter.writeIndent ();

		formatWriter.writeFormat (
			"<thead");

		htmlAttributesWrite (
			formatWriter,
			attributes);

		formatWriter.writeFormat (
			">");

		formatWriter.writeNewline ();

		formatWriter.increaseIndent ();

	}

	public static
	void htmlTableHeadClose (
			@NonNull FormatWriter formatWriter) {

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"</thead>");

	}

	// table body

	public static
	void htmlTableBodyOpen (
			@NonNull FormatWriter formatWriter,
			@NonNull ToHtmlAttribute ... attributes) {

		formatWriter.writeIndent ();

		formatWriter.writeFormat (
			"<tbody");

		htmlAttributesWrite (
			formatWriter,
			attributes);

		formatWriter.writeFormat (
			">");

		formatWriter.writeNewline ();

		formatWriter.increaseIndent ();

	}

	public static
	void htmlTableBodyClose (
			@NonNull FormatWriter formatWriter) {

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"</tbody>");

	}

	// table row

	public static
	void htmlTableRowOpen (
			@NonNull FormatWriter formatWriter,
			@NonNull Iterable <ToHtmlAttribute> attributes) {

		formatWriter.writeIndent ();

		formatWriter.writeFormat (
			"<tr");

		htmlAttributesWrite (
			formatWriter,
			attributes);

		formatWriter.writeFormat (
			">");

		formatWriter.writeNewline ();

		formatWriter.increaseIndent ();

	}

	public static
	void htmlTableRowOpen (
			@NonNull FormatWriter formatWriter,
			@NonNull ToHtmlAttribute ... attributes) {

		htmlTableRowOpen (
			formatWriter,
			Arrays.asList (
				attributes));

	}

	public static
	void htmlTableRowClose (
			@NonNull FormatWriter formatWriter) {

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"</tr>");

	}

	public static
	void htmlTableCellOpen (
			@NonNull FormatWriter formatWriter,
			@NonNull ToHtmlAttribute ... attributes) {

		formatWriter.writeIndent ();

		formatWriter.writeFormat (
			"<td");

		htmlAttributesWrite (
			formatWriter,
			attributes);

		formatWriter.writeFormat (
			">");

		formatWriter.writeNewline ();

		formatWriter.increaseIndent ();

	}

	public static
	void htmlTableCellClose (
			@NonNull FormatWriter formatWriter) {

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"</td>");

	}

	public static
	void htmlTableCellWrite (
			@NonNull FormatWriter formatWriter,
			@NonNull CharSequence content,
			@NonNull ToHtmlAttribute ... attributes) {

		formatWriter.writeIndent ();

		formatWriter.writeFormat (
			"<td");

		htmlAttributesWrite (
			formatWriter,
			attributes);

		formatWriter.writeFormat (
			">%h</td>",
			content);

		formatWriter.writeNewline ();

	}

	public static
	void htmlTableCellWrite (
			@NonNull FormatWriter formatWriter,
			@NonNull Runnable content,
			@NonNull HtmlAttribute ... attributes) {

		formatWriter.writeIndent ();

		formatWriter.writeFormat (
			"<td");

		htmlAttributesWrite (
			formatWriter,
			attributes);

		formatWriter.writeFormat (
			">");

		content.run ();

		formatWriter.writeFormat (
			"</td>");

		formatWriter.writeNewline ();

	}

	public static
	void htmlTableCellWriteFormat (
			@NonNull FormatWriter formatWriter,
			@NonNull String ... arguments) {

		formatWriter.writeLineFormat (
			"<td>%s</td>",
			stringFormatArray (
				arguments));

	}

	public static
	void htmlTableCellWriteHtml (
			@NonNull FormatWriter formatWriter,
			@NonNull CharSequence content,
			@NonNull ToHtmlAttribute ... attributes) {

		formatWriter.writeIndent ();

		formatWriter.writeFormat (
			"<td");

		htmlAttributesWrite (
			formatWriter,
			attributes);

		formatWriter.writeFormat (
			">%s</td>",
			content);

		formatWriter.writeNewline ();

	}

	public static
	void htmlTableCellWriteHtml (
			@NonNull FormatWriter formatWriter,
			@NonNull Runnable content,
			@NonNull ToHtmlAttribute ... attributes) {

		htmlTableCellOpen (
			formatWriter,
			attributes);

		content.run ();

		htmlTableCellClose (
			formatWriter);

	}

	public static
	void htmlTableHeaderCellWrite (
			@NonNull FormatWriter formatWriter,
			@NonNull CharSequence content,
			@NonNull ToHtmlAttribute ... attributes) {

		formatWriter.writeIndent ();

		formatWriter.writeFormat (
			"<th");

		htmlAttributesWrite (
			formatWriter,
			attributes);

		formatWriter.writeFormat (
			">%h</th>",
			content);

		formatWriter.writeNewline ();

	}

	public static
	void htmlTableDetailsRowWrite (
			@NonNull FormatWriter formatWriter,
			@NonNull String label,
			@NonNull String value,
			@NonNull ToHtmlAttribute ... attributes) {

		htmlTableRowOpen (
			formatWriter);

		htmlTableHeaderCellWrite (
			formatWriter,
			label);

		htmlTableCellWrite (
			formatWriter,
			value,
			attributes);

		htmlTableRowClose (
			formatWriter);

	}

	public static
	void htmlTableDetailsRowWriteHtml (
			@NonNull FormatWriter formatWriter,
			@NonNull CharSequence label,
			@NonNull CharSequence value,
			@NonNull ToHtmlAttribute ... attributes) {

		htmlTableRowOpen (
			formatWriter,
			attributes);

		htmlTableHeaderCellWrite (
			formatWriter,
			label);

		htmlTableCellWriteHtml (
			formatWriter,
			value);

		htmlTableRowClose (
			formatWriter);

	}

	public static
	void htmlTableDetailsRowWriteHtml (
			@NonNull FormatWriter formatWriter,
			@NonNull String label,
			@NonNull Runnable value,
			@NonNull ToHtmlAttribute ... attributes) {

		htmlTableRowOpen (
			formatWriter,
			attributes);

		htmlTableHeaderCellWrite (
			formatWriter,
			label);

		htmlTableCellWriteHtml (
			formatWriter,
			value);

		htmlTableRowClose (
			formatWriter);

	}

	public static
	void htmlTableDetailsRowWriteRaw (
			@NonNull FormatWriter formatWriter,
			@NonNull String label,
			@NonNull String rawValue) {

		htmlTableRowOpen (
			formatWriter);

		htmlTableHeaderCellWrite (
			formatWriter,
			label);

		formatWriter.writeString (
			rawValue);

		htmlTableRowClose (
			formatWriter);

	}

	public static
	void htmlTableDetailsRowWriteRaw (
			@NonNull FormatWriter formatWriter,
			@NonNull String label,
			@NonNull Consumer <FormatWriter> valueCellWriter) {

		htmlTableRowOpen (
			formatWriter);

		htmlTableHeaderCellWrite (
			formatWriter,
			label);

		valueCellWriter.accept (
			formatWriter);

		htmlTableRowClose (
			formatWriter);

	}

	public static
	void htmlTableDetailsRowWriteRaw (
			@NonNull FormatWriter formatWriter,
			@NonNull String label,
			@NonNull Runnable valueCellWriter) {

		htmlTableRowOpen (
			formatWriter);

		htmlTableHeaderCellWrite (
			formatWriter,
			label);

		valueCellWriter.run ();

		htmlTableRowClose (
			formatWriter);

	}

	public static
	void htmlTableRowSeparatorWrite (
			@NonNull FormatWriter formatWriter) {

		formatWriter.writeLineFormat (
			"<tr class=\"sep\">");

	}

}
