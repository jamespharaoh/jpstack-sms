package wbs.web.misc;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.NonNull;

import wbs.utils.string.FormatWriter;

public
class UrlParams {

	private final
	Map <String, List <String>> params =
		new LinkedHashMap <> ();

	public
	UrlParams () {
	}

	public
	UrlParams (
			@NonNull UrlParams oldUrlParams) {

		for (
			Map.Entry <String, List <String>> entry
				: oldUrlParams.params.entrySet ()
		) {

			String name =
				entry.getKey ();

			List <String> values =
				entry.getValue ();

			params.put (
				name,
				new ArrayList <String> (
					values));

		}

	}

	public
	UrlParams add (
			@NonNull String name,
			String value) {

		if (value == null)
			return this;

		List<String> values =
			params.get (name);

		if (values == null) {

			values =
				new ArrayList<String> ();

			params.put (
				name,
				values);

		}

		values.add (value);

		return this;

	}

	public
	UrlParams set (
			@NonNull String name,
			@NonNull String value) {

		List <String> values =
			new ArrayList <String> ();

		params.put (
			name,
			values);

		values.add (
			value);

		return this;

	}

	public
	UrlParams setSimpleMap (
			@NonNull Map <String, String> values) {

		for (
			Map.Entry <String, String> entry
				: values.entrySet ()
		) {

			set (
				entry.getKey (),
				entry.getValue ());

		}

		return this;

	}

	public
	void remove (
			String name) {

		params.remove (
			name);

	}

	public
	String toUrl (
			@NonNull String base,
			@NonNull String charset)
		throws UnsupportedEncodingException {

		StringBuilder stringBuilder =
			new StringBuilder (base);

		char sep = '?';

		for (
			Map.Entry <String, List <String>> entry
				: params.entrySet ()
		) {

			String name =
				entry.getKey ();

			List <String> values =
				entry.getValue ();

			for (
				String value
					: values
			) {

				stringBuilder.append (sep);

				stringBuilder.append (
					URLEncoder.encode (
						name,
						charset));

				stringBuilder.append ('=');

				stringBuilder.append (
					URLEncoder.encode (
						value,
						charset));

				sep = '&';

			}
		}

		return stringBuilder.toString ();

	}

	public
	String toUrl (
			@NonNull String base) {

		try {

			return toUrl (
				base,
				"utf8");

		} catch (UnsupportedEncodingException exception) {

			throw new RuntimeException (
				exception);

		}

	}

	public
	String toString (
			@NonNull String charset)
		throws UnsupportedEncodingException {

		StringBuilder stringBuilder =
			new StringBuilder ();

		boolean first = true;

		for (
			Map.Entry <String, List <String>> entry
				: params.entrySet ()
		) {

			String name =
				entry.getKey ();

			List <String> values =
				entry.getValue ();

			for (
				String value
					: values
			) {

				if (! first)
					stringBuilder.append ('&');

				stringBuilder.append (
					URLEncoder.encode (
						name,
						charset));

				stringBuilder.append ('=');

				stringBuilder.append (
					URLEncoder.encode (
						value,
						charset));

				first = false;

			}

		}

		return stringBuilder.toString ();

	}

	@Override
	public
	String toString () {

		try {

			return toString ("utf-8");

		} catch (UnsupportedEncodingException exception) {

			throw new RuntimeException (exception);

		}

	}

	public
	void printHidden (
			@NonNull FormatWriter formatWriter) {

		for (
			Map.Entry<String,List<String>> entry
				: params.entrySet ()
			) {

			String name =
				entry.getKey ();

			List<String> values =
				entry.getValue ();

			for (
				String value
					: values
			) {

				formatWriter.writeLineFormat (
					"<input",
					" type=\"hidden\"",
					" name=\"%h\"",
					name,
					" value=\"%h\"",
					value,
					">");

			}

		}

	}

	public static
	String simpleMapToString (
			@NonNull Map <String, String> paramsMap) {

		UrlParams urlParams =
			new UrlParams ();

		urlParams.setSimpleMap (
			paramsMap);

		return urlParams.toString ();

	}

	public static
	String mapToString (
			@NonNull Map <String, List <String>> paramsMap) {

		UrlParams urlParams =
			new UrlParams ();

		for (
			Map.Entry <String, List <String>> entry
				: paramsMap.entrySet ()
		) {

			String name =
				entry.getKey ();

			for (
				String value
					: entry.getValue ()
			) {

				urlParams.set (
					name,
					value);

			}

		}

		return urlParams.toString ();

	}

}
