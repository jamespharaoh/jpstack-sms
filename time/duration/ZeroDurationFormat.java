package wbs.utils.time.duration;

import static wbs.utils.etc.NumberUtils.equalToZero;
import static wbs.utils.etc.OptionalUtils.optionalIf;

import java.util.regex.Pattern;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.joda.time.Duration;
import org.joda.time.ReadableDuration;

public
class ZeroDurationFormat
	implements DurationFormat {

	@Override
	public
	Optional <Duration> match (
			@NonNull String input) {

		return optionalIf (
			zeroPattern.matcher (input).matches (),
			() -> Duration.ZERO);

	}

	@Override
	public
	Optional <String> textify (
			@NonNull ReadableDuration input) {

		long inputMilliseconds =
			input.getMillis ();

		return optionalIf (
			equalToZero (
				inputMilliseconds),
			() -> "0");

	}

	Pattern zeroPattern =
		Pattern.compile (
			"\\s*0+\\s*");

}
