package wbs.web.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import lombok.NonNull;

import wbs.utils.exception.RuntimeUnsupportedEncodingException;

public
class UrlUtils {

	public static
	String urlEncode (
			@NonNull String string) {

		try {

			return URLEncoder.encode (
				string,
				"utf8");

		} catch (UnsupportedEncodingException exception) {

			throw new RuntimeUnsupportedEncodingException (
				exception);

		}

	}

	public static
	String urlEncodeParameters (
			@NonNull Map <String, List <String>> parameters) {

		StringBuilder stringBuilder =
			new StringBuilder ();

		boolean first = true;

		for (
			Map.Entry <String, List <String>> entry
				: parameters.entrySet ()
		) {

			String name =
				entry.getKey ();

			List <String> values =
				entry.getValue ();

			for (
				String value
					: values
			) {

				if (! first) {
					stringBuilder.append ('&');
				}

				stringBuilder.append (
					urlEncode (
						name));

				stringBuilder.append ('=');

				stringBuilder.append (
					urlEncode (
						value));

				first = false;

			}

		}

		return stringBuilder.toString ();

	}

}
