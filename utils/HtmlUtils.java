package wbs.web.utils;

import static wbs.utils.string.StringUtils.replaceAll;
import static wbs.web.utils.HtmlAttributeUtils.htmlAttributesWrite;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Map;

import lombok.NonNull;

import wbs.utils.string.FormatWriter;

import wbs.web.utils.HtmlAttributeUtils.HtmlAttribute;
import wbs.web.utils.HtmlAttributeUtils.ToHtmlAttribute;

public
class HtmlUtils {

	public static
	String htmlEncode (
			@NonNull String source) {

		StringBuilder dest =
			new StringBuilder (
				source.length () * 2);

		for (
			int index = 0;
			index < source.length ();
			index ++
		) {

			char character =
				source.charAt (
					index);

			switch (character) {

			case '<':

				dest.append (
					"&lt;");

				break;

			case '>':

				dest.append (
					"&gt;");

				break;

			case '&':

				dest.append (
					"&amp;");

				break;

			case '"':

				dest.append (
					"&quot;");

				break;

			default:

				if (character < 128) {

					dest.append (
						character);

				} else {

					dest.append (
						"&#");

					dest.append (
						(int) character);

					dest.append (
						';');

				}

			}

		}

		return dest.toString ();

	}

	public static
	String htmlEncodeSimple (
			@NonNull String source) {

		StringBuilder dest =
			new StringBuilder (
				source.length () * 2);

		for (
			int index = 0;
			index < source.length ();
			index ++
		) {

			char character =
				source.charAt (
					index);

			switch (character) {

			case '<':

				dest.append (
					"&lt;");

				break;

			case '&':

				dest.append (
					"&amp;");

				break;

			default:

				dest.append (
					character);

			}

		}

		return dest.toString ();

	}

	public static
	String javascriptStringEscape (
			String source) {

		if (source == null)
			return null;

		StringBuilder dest =
			new StringBuilder (
				source.length () * 2);

		for (
			int pos = 0;
			pos < source.length ();
			pos++
		) {

			char ch =
				source.charAt (pos);

			switch (ch) {

			case '"':
				dest.append ("\\\"");
				break;

			case '\'':
				dest.append ("\\'");
				break;

			case '\n':
				dest.append ("\\n");
				break;

			case '\r':
				dest.append ("\\r");
				break;

			case '/':
				dest.append ("\\/");
				break;

			case '\\':
				dest.append ("\\\\");
				break;

			default:
				dest.append (ch);

			}

		}

		return dest.toString ();

	}

	public static
	String htmlJavascriptEncode (
			@NonNull String string) {

		return htmlEncode (
			javascriptStringEscape (
				string));

	}

	public static
	String htmlNonBreakingWhitespace (
			String source) {

		return source.replaceAll (
			" ",
			"&nbsp;");

	}

	public static
	String htmlEncodeNonBreakingWhitespace (
			String source) {

		return htmlNonBreakingWhitespace (
			htmlEncode (
				source));

	}

	public static
	String htmlNewlineToBr (
			String source) {

		return source.replaceAll (
			"\r\n|\n|\r",
			"<br>");

	}

	public static
	String htmlEncodeNewlineToBr (
			@NonNull String source) {

		return htmlNewlineToBr (
			htmlEncode (
				source));

	}

	public static
	String htmlEncodeSimpleNewlineToBr (
			@NonNull String source) {

		return htmlNewlineToBr (
			htmlEncodeSimple (
				source));

	}

	public static
	String urlQueryParameterEncode (
			@NonNull String source) {

		try {

			return URLEncoder.encode (
				source,
				"utf-8");

		} catch (UnsupportedEncodingException exception) {

			throw new RuntimeException (
				exception);

		}

	}

	public static
	String urlPathElementEncode (
			@NonNull String source) {

		try {

			return replaceAll (
				URLEncoder.encode (
					source,
					"utf-8"),
				"+",
				"%20");

		} catch (UnsupportedEncodingException exception) {

			throw new RuntimeException (
				exception);

		}
	}

	public static
	String htmlColourFromInteger (
			@NonNull Long number) {

		return String.format (
			"#%06x",
			number & 0x0000000000ffffffl);

	}

	public static
	String htmlColourFromObject (
			@NonNull Object object) {

		return htmlColourFromInteger (
			(long) (int)
			object.hashCode ());

	}

	public static
	void htmlWriteAttributesFromMap (
			@NonNull FormatWriter formatWriter,
			@NonNull Map <String, String> map) {

		for (
			Map.Entry <String, String> entry
				: map.entrySet ()
		) {

			formatWriter.writeFormat (
				"%s=\"%h\"",
				entry.getKey (),
				entry.getValue ());

		}

	}

	// html link

	public static
	void htmlLinkWriteInline (
			@NonNull FormatWriter formatWriter,
			@NonNull String href,
			@NonNull String content,
			@NonNull Iterable <ToHtmlAttribute> attributes) {

		formatWriter.writeFormat (
			"<a href=\"%h\"",
			href);

		htmlAttributesWrite (
			formatWriter,
			attributes);

		formatWriter.writeFormat (
			">%h</a>",
			content);

	}

	public static
	void htmlLinkWriteInline (
			@NonNull FormatWriter formatWriter,
			@NonNull String href,
			@NonNull String content,
			@NonNull ToHtmlAttribute ... attributes) {

		htmlLinkWriteInline (
			formatWriter,
			href,
			content,
			Arrays.asList (
				attributes));

	}

	public static
	void htmlLinkWrite (
			@NonNull FormatWriter formatWriter,
			@NonNull String href,
			@NonNull String content,
			@NonNull Iterable <ToHtmlAttribute> attributes) {

		formatWriter.writeIndent ();

		formatWriter.writeFormat (
			"<a href=\"%h\"",
			href);

		htmlAttributesWrite (
			formatWriter,
			attributes);

		formatWriter.writeFormat (
			">%h</a>",
			content);

		formatWriter.writeNewline ();

	}

	public static
	void htmlLinkWrite (
			@NonNull FormatWriter formatWriter,
			@NonNull String href,
			@NonNull String content,
			@NonNull HtmlAttribute ... attributes) {

		formatWriter.writeIndent ();

		formatWriter.writeFormat (
			"<a href=\"%h\"",
			href);

		htmlAttributesWrite (
			formatWriter,
			attributes);

		formatWriter.writeFormat (
			">%h</a>",
			content);

		formatWriter.writeNewline ();

	}

	public static
	void htmlLinkWriteHtml (
			@NonNull FormatWriter formatWriter,
			@NonNull String href,
			@NonNull String content,
			@NonNull HtmlAttribute ... attributes) {

		formatWriter.writeIndent ();

		formatWriter.writeFormat (
			"<a href=\"%h\"",
			href);

		htmlAttributesWrite (
			formatWriter,
			attributes);

		formatWriter.writeFormat (
			">%s</a>",
			content);

		formatWriter.writeNewline ();

	}

	public static
	void htmlLinkWriteHtml (
			@NonNull FormatWriter formatWriter,
			@NonNull String href,
			@NonNull Runnable content,
			@NonNull HtmlAttribute ... attributes) {

		formatWriter.writeIndent ();

		formatWriter.writeFormat (
			"<a href=\"%h\"",
			href);

		htmlAttributesWrite (
			formatWriter,
			attributes);

		formatWriter.writeFormat (
			">");

		content.run ();

		formatWriter.writeFormat (
			"</a>");

		formatWriter.writeNewline ();

	}

}
