package wbs.web.utils;

import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.string.FormatWriterUtils.formatWriterConsumerToString;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringToUtf8;

import java.util.NoSuchElementException;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import lombok.Builder;
import lombok.NonNull;
import lombok.experimental.Accessors;

import org.json.simple.JSONValue;

import wbs.utils.string.FormatWriter;

public
class JsonUtils {

	public static
	byte[] jsonToBytes (
			@NonNull Object object) {

		return stringToUtf8 (
			JSONValue.toJSONString (
				object));

	}

	public static
	String escapeJsonString (
			@NonNull EscapeOptions escapeOptions,
			@NonNull String source) {

		return formatWriterConsumerToString (
			"  ",
			formatWriter ->
				writeEscapedJsonString (
					formatWriter,
					escapeOptions,
					source));

	}

	public static
	void writeEscapedJsonString (
			@NonNull FormatWriter formatWriter,
			@NonNull EscapeOptions escapeOptions,
			@NonNull String source) {

		source.chars ().forEach (
			character -> {

			switch (character) {

			case '"':

				formatWriter.writeString (
					escapeOptions.doubleQuote);

				break;

			case '\'':

				formatWriter.writeString (
					escapeOptions.singleQuote);

				break;

			case '\n':

				formatWriter.writeString (
					"\\n");

				break;

			case '\r':

				formatWriter.writeString (
					"\\r");

				break;

			case '/':

				formatWriter.writeString (
					escapeOptions.forwardSlash);

				break;

			case '\\':

				formatWriter.writeString (
					"\\\\");

				break;

			case '<':

				formatWriter.writeString (
					escapeOptions.lessThan);

				break;

			case '>':

				formatWriter.writeString (
					escapeOptions.greaterThan);

				break;

			case '&':

				formatWriter.writeString (
					escapeOptions.ampersand);

				break;

			default:

				formatWriter.writeCharacter (
					character);

			}

		});

	}

	public static
	String jsonObjectGetString (
			@NonNull JsonObject jsonObject,
			@NonNull String key) {

		JsonElement valueElement =
			jsonObject.get (
				key);

		if (
			isNull (
				valueElement)
		) {

			throw new NoSuchElementException (
				stringFormat (
					"No such element: %s",
					key));

		}

		if (! valueElement.isJsonPrimitive ()) {

			throw new ClassCastException (
				stringFormat (
					"Element is not a primitive: %s",
					key));

		}

		JsonPrimitive valuePrimitive =
			valueElement.getAsJsonPrimitive ();

		if (! valuePrimitive.isString ()) {

			throw new ClassCastException (
				stringFormat (
					"Element is not a string: %s",
					key));

		}

		return valuePrimitive.getAsString ();

	}

	public static
	Long jsonObjectGetInteger (
			@NonNull JsonObject jsonObject,
			@NonNull String key) {

		JsonElement valueElement =
			jsonObject.get (
				key);

		if (
			isNull (
				valueElement)
		) {

			throw new NoSuchElementException (
				stringFormat (
					"No such element: %s",
					key));

		}

		if (! valueElement.isJsonPrimitive ()) {

			throw new ClassCastException (
				stringFormat (
					"Element is not a primitive: %s",
					key));

		}

		JsonPrimitive valuePrimitive =
			valueElement.getAsJsonPrimitive ();

		if (! valuePrimitive.isNumber ()) {

			throw new ClassCastException (
				stringFormat (
					"Element is not a number: %s",
					key));

		}

		return valuePrimitive.getAsLong ();

	}

	public static
	JsonObject jsonObjectGetObject (
			@NonNull JsonObject jsonObject,
			@NonNull String key) {

		JsonElement valueElement =
			jsonObject.get (
				key);

		if (
			isNull (
				valueElement)
		) {

			throw new NoSuchElementException (
				stringFormat (
					"No such element: %s",
					key));

		}

		if (! valueElement.isJsonObject ()) {

			throw new ClassCastException (
				stringFormat (
					"Element is not an object: %s",
					key));

		}

		return valueElement.getAsJsonObject ();

	}

	public static
	JsonObject jsonObjectParse (
			@NonNull String string) {

		JsonParser jsonParser =
			new JsonParser ();

		JsonElement jsonElement =
			jsonParser.parse (
				string);

		return jsonElement.getAsJsonObject ();

	}

	public static
	String jsonEncode (
			@NonNull JsonElement jsonElement) {

		return jsonElement.toString ();

	}

	// options class

	@Accessors (fluent = true)
	@Builder
	static
	class EscapeOptions {

		String singleQuote;
		String doubleQuote;

		String forwardSlash;

		String lessThan;
		String greaterThan;
		String ampersand;

	}

	// default options

	public final static
	EscapeOptions defaultOptions =
		EscapeOptions.builder ()
			.singleQuote ("\\'")
			.doubleQuote ("\\\"")
			.forwardSlash ("/")
			.lessThan ("<")
			.greaterThan (">")
			.ampersand ("&")
			.build ();

	public final static
	EscapeOptions defaultDoubleQuotesOptions =
		EscapeOptions.builder ()
			.singleQuote ("'")
			.doubleQuote ("\\\"")
			.forwardSlash ("/")
			.lessThan ("<")
			.greaterThan (">")
			.ampersand ("&")
			.build ();

	public final static
	EscapeOptions defaultSingleQuotesOptions =
		EscapeOptions.builder ()
			.singleQuote ("\\'")
			.doubleQuote ("\"")
			.forwardSlash ("/")
			.lessThan ("<")
			.greaterThan (">")
			.ampersand ("&")
			.build ();

	// html options

	public final static
	EscapeOptions htmlDefaultOptions =
		EscapeOptions.builder ()
			.singleQuote ("\\'")
			.doubleQuote ("\\\"")
			.forwardSlash ("/")
			.lessThan ("&lt;")
			.greaterThan ("&gt;")
			.ampersand ("&amp;")
			.build ();

	public final static
	EscapeOptions htmlDoubleQuotesOptions =
		EscapeOptions.builder ()
			.singleQuote ("'")
			.doubleQuote ("\\\"")
			.forwardSlash ("/")
			.lessThan ("&lt;")
			.greaterThan ("&gt;")
			.ampersand ("&amp;")
			.build ();

	public final static
	EscapeOptions htmlSingleQuotesOptions =
		EscapeOptions.builder ()
			.singleQuote ("\\'")
			.doubleQuote ("\"")
			.forwardSlash ("/")
			.lessThan ("&lt;")
			.greaterThan ("&gt;")
			.ampersand ("&amp;")
			.build ();

	// html script options

	public final static
	EscapeOptions htmlScriptDefaultOptions =
		EscapeOptions.builder ()
			.singleQuote ("\\'")
			.doubleQuote ("\\\"")
			.forwardSlash ("\\/")
			.lessThan ("<")
			.greaterThan (">")
			.ampersand ("&")
			.build ();

	public final static
	EscapeOptions htmlScriptDoubleQuotesOptions =
		EscapeOptions.builder ()
			.singleQuote ("'")
			.doubleQuote ("\\\"")
			.forwardSlash ("\\/")
			.lessThan ("<")
			.greaterThan (">")
			.ampersand ("&")
			.build ();

	public final static
	EscapeOptions htmlScriptSingleQuotesOptions =
		EscapeOptions.builder ()
			.singleQuote ("\\'")
			.doubleQuote ("\"")
			.forwardSlash ("\\/")
			.lessThan ("<")
			.greaterThan (">")
			.ampersand ("&")
			.build ();

}
