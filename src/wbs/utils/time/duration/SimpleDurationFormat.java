package wbs.utils.time.duration;

import static wbs.utils.etc.NumberUtils.parseIntegerRequired;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.OptionalUtils.optionalOfFormat;
import static wbs.utils.string.StringUtils.pluralise;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.joda.time.Duration;
import org.joda.time.ReadableDuration;

public class SimpleDurationFormat
	implements DurationFormat {

	String singularLabel;
	String pluralLabel;

	Pattern pattern;

	long scale;

	SimpleDurationFormat (
			@NonNull Long newScale,
			@NonNull String newSingularLabel,
			@NonNull String newPluralLabel,
			@NonNull String ... otherLabels) {

		scale =
			newScale;

		singularLabel =
			newSingularLabel;

		pluralLabel =
		newPluralLabel;

		StringBuilder stringBuilder =
			new StringBuilder ();

		stringBuilder.append (
			"\\s*(\\d+)\\s*(");

		stringBuilder.append (
			Pattern.quote (
				singularLabel));

		if (pluralLabel != null && ! singularLabel.equals (pluralLabel)) {

			stringBuilder.append (
				'|');

			stringBuilder.append (
				Pattern.quote (
					pluralLabel));

		}

		for (
			String otherLabel
				: otherLabels
		) {

			stringBuilder.append (
				'|');

			stringBuilder.append (
				Pattern.quote (
					otherLabel));

		}

		stringBuilder.append (
			")\\s*");

		pattern =
			Pattern.compile (
				stringBuilder.toString ());

	}

	@Override
	public
	Optional <Duration> match (
			@NonNull String input) {

		Matcher matcher =
			pattern.matcher (
				input);

		if (! matcher.matches ()) {
			return optionalAbsent ();
		}

		return optionalOf (
			Duration.millis (
				scale * parseIntegerRequired (
					matcher.group (1))));

	}

	@Override
	public
	Optional <String> textify (
			@NonNull ReadableDuration input) {

		long inputMilliseconds =
			input.getMillis ();

		if (inputMilliseconds % scale != 0) {

			return optionalAbsent ();

		} else {

			return optionalOfFormat (
				pluralise (
					inputMilliseconds / scale,
					singularLabel,
					pluralLabel));

		}

	}

}
