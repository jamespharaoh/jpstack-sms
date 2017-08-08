package wbs.web.utils;

import static wbs.utils.etc.OptionalUtils.optionalEqualOrNotPresentSafe;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.string.StringUtils.stringEqualSafe;
import static wbs.utils.string.StringUtils.stringIsEmpty;
import static wbs.utils.string.StringUtils.stringIsNotEmpty;
import static wbs.web.utils.HtmlAttributeUtils.htmlAttributesWrite;

import java.util.Map;

import lombok.NonNull;

import wbs.utils.string.FormatWriter;

import wbs.web.utils.HtmlAttributeUtils.HtmlAttribute;
import wbs.web.utils.HtmlAttributeUtils.ToHtmlAttribute;

public
class HtmlInputUtils {

	// select elements

	public static
	void htmlSelectOpen (
			@NonNull FormatWriter formatWriter,
			@NonNull HtmlAttribute ... attributes) {

		formatWriter.writeIndent ();

		formatWriter.writeFormat (
			"<select");

		htmlAttributesWrite (
			formatWriter,
			attributes);

		formatWriter.writeFormat (
			">");

		formatWriter.writeNewline ();

		formatWriter.increaseIndent ();

	}

	public static
	void htmlSelectOpen (
			@NonNull FormatWriter formatWriter,
			@NonNull String name,
			@NonNull HtmlAttribute ... attributes) {

		formatWriter.writeIndent ();

		formatWriter.writeFormat (
			"<select",
			" name=\"%h\"",
			name);

		htmlAttributesWrite (
			formatWriter,
			attributes);

		formatWriter.writeFormat (
			">");

		formatWriter.writeNewline ();

		formatWriter.increaseIndent ();

	}

	public static
	void htmlSelectClose (
			@NonNull FormatWriter formatWriter) {

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"</select>");

	}

	// option elements

	public static
	void htmlOptionWrite (
			@NonNull FormatWriter formatWriter,
			@NonNull String value,
			@NonNull Boolean selected,
			@NonNull String content,
			@NonNull HtmlAttribute ... attributes) {

		formatWriter.writeIndent ();

		formatWriter.writeFormat (
			"<option");

		if (
			stringIsNotEmpty (
				value)
		) {

			formatWriter.writeFormat (
				" value=\"%h\"",
				value);

		}

		if (selected) {

			formatWriter.writeFormat (
				" selected");

		}

		htmlAttributesWrite (
			formatWriter,
			attributes);

		formatWriter.writeFormat (
			">%h</option>",
			content);

		formatWriter.writeNewline ();

	}

	public static
	void htmlOptionWrite (
			@NonNull FormatWriter formatWriter,
			@NonNull String value,
			@NonNull String selectedValue,
			@NonNull String content,
			@NonNull HtmlAttribute ... attributes) {

		htmlOptionWrite (
			formatWriter,
			value,
			stringEqualSafe (
				value,
				selectedValue),
			content,
			attributes);

	}

	public static
	void htmlOptionWrite (
			@NonNull FormatWriter formatWriter,
			@NonNull HtmlAttribute ... attributes) {

		htmlOptionWrite (
			formatWriter,
			"",
			false,
			"",
			attributes);

	}

	public static
	void htmlOptionWrite (
			@NonNull FormatWriter formatWriter,
			@NonNull String value,
			@NonNull HtmlAttribute ... attributes) {

		htmlOptionWrite (
			formatWriter,
			value,
			false,
			value,
			attributes);

	}

	public static
	void htmlOptionWrite (
			@NonNull FormatWriter formatWriter,
			@NonNull String value,
			@NonNull String content,
			@NonNull HtmlAttribute ... attributes) {

		htmlOptionWrite (
			formatWriter,
			value,
			false,
			content,
			attributes);

	}

	public static
	void htmlOptionWriteSelected (
			@NonNull FormatWriter formatWriter,
			@NonNull String value,
			@NonNull HtmlAttribute ... attributes) {

		htmlOptionWrite (
			formatWriter,
			value,
			true,
			value,
			attributes);

	}

	// select elements with options

	public static
	void htmlSelectYesNo (
			@NonNull FormatWriter formatWriter,
			@NonNull String name,
			@NonNull Boolean value,
			@NonNull String trueText,
			@NonNull String falseText) {

		formatWriter.writeLineFormat (
			"<select",
			" id=\"%h\"",
			name,
			" name=\"%h\"",
			name,
			">");

		formatWriter.increaseIndent ();

		// no option

		formatWriter.writeIndent ();

		formatWriter.writeFormat (
			"<option",
			" value=\"false\"");

		if (! value) {

			formatWriter.writeFormat (
				" selected");

		}

		formatWriter.writeFormat (
			">%h</option>",
			falseText);

		formatWriter.writeNewline ();

		// yes option

		formatWriter.writeIndent ();

		formatWriter.writeFormat (
			"<option value=\"true\"");

		if (value) {

			formatWriter.writeFormat (
				" selected");

		}

		formatWriter.writeFormat (
			">%h</option>",
			trueText);

		formatWriter.writeNewline ();

		// close select

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"</select>");

	}

	public static
	void htmlSelectYesNo (
			@NonNull FormatWriter formatWriter,
			@NonNull String name,
			@NonNull Boolean value) {

		htmlSelectYesNo (
			formatWriter,
			name,
			value,
			"yes",
			"no");

	}

	public static
	void htmlSelectYesNo (
			@NonNull FormatWriter formatWriter,
			@NonNull String name,
			@NonNull String formValue,
			@NonNull Boolean defaultValue) {

		// interpret form value

		boolean value;

		if (
			stringIsEmpty (
				formValue)
		) {

			value =
				defaultValue;

		} else if (
			stringEqualSafe (
				formValue,
				"true")
		) {

			value =
				true;

		} else if (
			stringEqualSafe (
				formValue,
				"false")
		) {

			value =
				false;

		} else {

			throw new RuntimeException (
				"Invalid form value: " + formValue);

		}

		// render control

		htmlSelectYesNo (
			formatWriter,
			name,
			value,
			"yes",
			"no");

	}

	public static
	void htmlSelectYesNoMaybe (
			@NonNull FormatWriter formatWriter,
			@NonNull String name,
			@NonNull String value) {

		formatWriter.writeLineFormat (
			"<select name=\"%h\">",
			name);

		formatWriter.increaseIndent ();

		htmlOption (
			formatWriter,
			"",
			"—",
			value);

		htmlOption (
			formatWriter,
			"true",
			"yes",
			value);

		htmlOption (
			formatWriter,
			"false",
			"no",
			value);

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"</select>");

	}

	public static <Key>
	void htmlSelect (
			@NonNull FormatWriter formatWriter,
			@NonNull String name,
			@NonNull Map <? extends Key, String> options,
			@NonNull Key selectedValue,
			@NonNull ToHtmlAttribute ... attributes) {

		formatWriter.writeIndent ();

		formatWriter.writeFormat (
			"<select",
			" id=\"%h\"",
			name,
			" name=\"%h\"",
			name);

		htmlAttributesWrite (
			formatWriter,
			attributes);

		formatWriter.writeFormat (
			">");

		formatWriter.writeNewline ();

		formatWriter.increaseIndent ();

		for (
			Map.Entry <? extends Key, String> optionEntry
				: options.entrySet ()
		) {

			htmlOption (
				formatWriter,
				optionEntry.getKey (),
				optionEntry.getValue (),
				selectedValue);

		}

		formatWriter.decreaseIndent ();

		formatWriter.writeLineFormat (
			"</select>");

	}

	public static <Key>
	void htmlOption (
			@NonNull FormatWriter formatWriter,
			@NonNull Key value,
			@NonNull String label,
			@NonNull Key defaultValue) {

		boolean selected =
			optionalEqualOrNotPresentSafe (
				optionalFromNullable (
					value),
				optionalFromNullable (
					defaultValue));

		boolean gotValue =
			value != null
			&& ! value.toString ().isEmpty ();

		boolean gotLabel =
			label != null
			&& ! label.isEmpty ();

		formatWriter.writeIndent ();

		formatWriter.writeFormat (
			"<option");

		if (gotValue) {

			formatWriter.writeFormat (
				" value=\"%h\"",
				value.toString ());

		}

		if (selected) {

			formatWriter.writeFormat (
				" selected");

		}

		formatWriter.writeFormat (
			">");

		if (gotLabel) {

			formatWriter.writeFormat (
				"%h</option>",
				label);

		}

		formatWriter.writeNewline ();

	}

	// radio input elements

	public static
	void htmlRadio (
			@NonNull FormatWriter formatWriter,
			@NonNull String name,
			@NonNull String value,
			@NonNull Boolean selected,
			@NonNull ToHtmlAttribute ... attributes) {

		formatWriter.writeIndent ();

		formatWriter.writeFormat (
			"<input",
			" type=\"radio\"",
			" name=\"%h\"",
			name,
			" value=\"%s\"",
			value);

		if (selected) {

			formatWriter.writeFormat (
				" selected");

		}

		htmlAttributesWrite (
			formatWriter,
			attributes);

		formatWriter.writeFormat (
			">");

		formatWriter.writeNewline ();

	}
}
