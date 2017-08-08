package wbs.web.utils;

import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.web.utils.JsonUtils.writeEscapedJsonString;

import java.util.Map;
import java.util.stream.LongStream;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import wbs.utils.string.FormatWriter;

@Accessors (fluent = true)
public
class JsonToHtml {

	// properties

	@Getter @Setter
	long indent = 0;

	@Getter @Setter
	String indentString = "  ";

	@Getter @Setter
	FormatWriter formatWriter;

	@Getter @Setter
	Boolean escapeSlashes = false;

	// implementation

	public
	void write (
			@NonNull Object object) {

		if (object instanceof JSONObject) {

			writeJsonObject (
				(JSONObject)
				object);

		} else if (object instanceof JSONArray) {

			writeJsonArray (
				(JSONArray)
				object);

		} else if (object instanceof String) {

			writeString (
				(String)
				object);

		} else if (object instanceof Long) {

			writeInteger (
				(Long)
				object);

		} else {

			throw new RuntimeException (
				stringFormat (
					"Unable to encode %s",
					object.getClass ().getSimpleName ()));

		}

	}

	public
	void writeJsonObject (
			@NonNull JSONObject jsonObject) {

		if (jsonObject.isEmpty ()) {

			formatWriter.writeFormat (
				"{}");

		} else {

			formatWriter.writeString (
				"{");

			indent ++;

			for (
				Object entryObject
					: jsonObject.entrySet ()
			) {

				Map.Entry<?,?> entry =
					(Map.Entry<?,?>) entryObject;

				writeNewlineAndIndent ();

				formatWriter.writeString (
					(String)
					entry.getKey ());

				formatWriter.writeString (
					": ");

				write (
					entry.getValue ());

			}

			indent --;

			writeNewlineAndIndent ();

			formatWriter.writeString (
				"}");

		}

	}

	public
	void writeJsonArray (
			@NonNull JSONArray jsonArray) {

		if (jsonArray.isEmpty ()) {

			formatWriter.writeString (
				"[]");

		} else {

			formatWriter.writeString (
				"[");

			indent ++;

			for (
				Object item
					: jsonArray
			) {

				writeNewlineAndIndent ();

				write (
					item);

			}

			indent --;

			writeNewlineAndIndent ();

			formatWriter.writeString (
				"]");

		}

	}

	public
	void writeString (
			@NonNull String string) {

		formatWriter.writeString (
			"\"");

		if (escapeSlashes) {

			writeEscapedJsonString (
				formatWriter,
				JsonUtils.htmlScriptDoubleQuotesOptions,
				string);

		} else {

			writeEscapedJsonString (
				formatWriter,
				JsonUtils.htmlDoubleQuotesOptions,
				string);

		}

		formatWriter.writeString (
			"\"");

	}

	public
	void writeInteger (
			@NonNull Long integer) {

		formatWriter.writeString (
			Long.toString (
				integer));

	}

	// private implementation

	private
	void writeNewlineAndIndent () {

		formatWriter.writeString (
			"\n");

		writeIndent ();

	}

	private
	void writeIndent () {

		LongStream.range (0, indent).forEach (
			index ->
				formatWriter.writeString (
					indentString));

	}

}
