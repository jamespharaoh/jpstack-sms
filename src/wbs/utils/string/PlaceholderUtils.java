package wbs.utils.string;

import static wbs.utils.collection.MapUtils.mapItemForKeyRequired;
import static wbs.utils.string.StringUtils.substring;
import static wbs.utils.string.StringUtils.substringFrom;

import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

public
class PlaceholderUtils {

	public static
	String placeholderMap (
			@NonNull Pattern pattern,
			@NonNull String template,
			@NonNull Function <String, String> lookupFunction) {

		Matcher matcher =
			pattern.matcher (
				template);

		StringBuilder valueBuilder =
			new StringBuilder ();

		int position = 0;

		while (matcher.find ()) {

			valueBuilder.append (
				substring (
					template,
					position,
					matcher.start ()));

			String placeholderName =
				matcher.group (1);

			String placeholderValue =
				lookupFunction.apply (
					placeholderName);

			valueBuilder.append (
				placeholderValue);

			position =
				matcher.end ();

		}

		valueBuilder.append (
			substringFrom (
				template,
				position));

		return valueBuilder.toString ();

	}

	public static
	String placeholderMap (
			@NonNull Pattern pattern,
			@NonNull String template,
			@NonNull Map <String, String> values) {

		return placeholderMap (
			pattern,
			template,
			key ->
				mapItemForKeyRequired (
					values,
					key));

	}

	public static
	String placeholderMapCurlyBraces (
			@NonNull String template,
			@NonNull Map <String, String> values) {

		return placeholderMap (
			curlyBracesPlaceholderPattern,
			template,
			key ->
				mapItemForKeyRequired (
					values,
					key));

	}

	public static
	Map <String, String> placeholderMap (
			@NonNull Pattern pattern,
			@NonNull Map <String, String> templates,
			@NonNull Function <String, String> lookupFunction) {

		ImmutableMap.Builder <String, String> builder =
			ImmutableMap.builder ();

		for (
			Map.Entry <String, String> templateEntry
				: templates.entrySet ()
		) {

			builder.put (
				templateEntry.getKey (),
				placeholderMap (
					pattern,
					templateEntry.getValue (),
					lookupFunction));

		}

		return builder.build ();

	}

	public static
	Map <String, String> placeholderMapCurlyBraces (
			@NonNull Map <String, String> templates,
			@NonNull Function <String, String> lookupFunction) {

		return placeholderMap (
			curlyBracesPlaceholderPattern,
			templates,
			lookupFunction);

	}

	public static
	Map <String, String> placeholderMap (
			@NonNull Pattern pattern,
			@NonNull Map <String, String> templates,
			@NonNull Map <String, String> values) {

		return placeholderMap (
			pattern,
			templates,
			key ->
				mapItemForKeyRequired (
					values,
					key));

	}

	public static
	Map <String, String> placeholderMapCurlyBraces (
			@NonNull Map <String, String> templates,
			@NonNull Map <String, String> values) {

		return placeholderMap (
			curlyBracesPlaceholderPattern,
			templates,
			key ->
				mapItemForKeyRequired (
					values,
					key));

	}

	// data

	private final static
	Pattern curlyBracesPlaceholderPattern =
		Pattern.compile (
			"\\{([^}]+)\\}");

}
