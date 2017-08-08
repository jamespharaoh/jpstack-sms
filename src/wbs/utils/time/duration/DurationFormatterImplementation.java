package wbs.utils.time.duration;

import static wbs.utils.etc.NumberUtils.lessThanZero;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.pluralise;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.time.TimeUtils.millisecondsToDuration;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.joda.time.Duration;
import org.joda.time.ReadableDuration;

import wbs.framework.component.annotations.SingletonComponent;

@SingletonComponent ("durationFormatter")
public
class DurationFormatterImplementation
	implements DurationFormatter {

	// duration to string

	@Override
	public
	String durationStringNumeric (
			@NonNull ReadableDuration inputDuration) {

		long inputMilliseconds =
			inputDuration.getMillis ();

		if (inputMilliseconds < 60l * 1000l) {

			if (inputMilliseconds % 1000l != 0) {

				return String.format (
					"%d.%03d",
					inputMilliseconds / 1000l,
					inputMilliseconds % 1000l);

			} else {

				return String.format (
					"%d",
					inputMilliseconds / 1000l);

			}

		} else if (inputMilliseconds < 60l * 60l * 1000l) {

			if (inputMilliseconds % 1000l != 0) {

				return String.format (
					"%d:%02d.%03d",
					inputMilliseconds / 1000l / 60l,
					inputMilliseconds / 1000l % 60l,
					inputMilliseconds % 1000l);

			} else {

				return String.format (
					"%d:%02d",
					inputMilliseconds / 1000l / 60l,
					inputMilliseconds / 1000l % 60l);

			}

		} else {

			if (inputMilliseconds % 1000l != 0) {

				return String.format (
					"%d:%02d:%02d.%03d",
					inputMilliseconds / 1000l / 60l / 60l,
					inputMilliseconds / 1000l / 60l % 60l,
					inputMilliseconds / 1000l % 60l,
					inputMilliseconds % 1000l);

			} else {

				return String.format (
					"%d:%02d:%02d",
					inputMilliseconds / 1000l / 60l / 60l,
					inputMilliseconds / 1000l / 60l % 60l,
					inputMilliseconds / 1000l % 60l);

			}

		}

	}

	@Override
	public
	String durationStringExact (
			@NonNull ReadableDuration input) {

		for (
			DurationFormat intervalMatcher
				: durationFormats
		) {

			Optional <String> intervalStringOptional =
				intervalMatcher.textify (
					input);

			if (
				optionalIsNotPresent (
					intervalStringOptional)
			) {
				continue;
			}

			return optionalGetRequired (
				intervalStringOptional);

		}

		throw new RuntimeException ();

	}

	@Override
	public
	String durationStringApproximate (
			@NonNull ReadableDuration duration) {

		long milliseconds =
			duration.getMillis ();

		if (
			lessThanZero (
				milliseconds)
		) {

			return stringFormat (
				"-%s",
				durationStringExact (
					millisecondsToDuration (
						- milliseconds)));

		}

		if (milliseconds < 2 * 1000L) {

			return pluralise (
				milliseconds,
				"millisecond");

		} else if (milliseconds < 2 * 60000L) {

			return pluralise (
				milliseconds / 1000L,
				"second");

		} else if (milliseconds < 2 * 3600000L) {

			return pluralise (
				milliseconds / 60000L,
				"minute");

		} else if (milliseconds < 2 * 86400000L) {

			return pluralise (
				milliseconds / 3600000L,
				"hour");

		} else if (milliseconds < 2 * 2678400000L) {

			return pluralise (
				milliseconds / 86400000L,
				"day");

		} else if (milliseconds < 2 * 31557600000L) {

			return pluralise (
				milliseconds / 2592000000L,
				"month");

		} else {

			return pluralise (
				milliseconds / 31556736000L,
				"year");

		}

	}

	// string to duration

	@Override
	public
	Optional <Duration> stringToDuration (
			@NonNull String input) {

		for (
			DurationFormat durationFormat
				: durationFormats
		) {

			Optional <Duration> durationOptional =
				durationFormat.match (
					input);

			if (
				optionalIsNotPresent (
					durationOptional)
			) {
				continue;
			}

			Duration duration =
				optionalGetRequired (
					durationOptional);

			return optionalOf (
				duration);

		}

		return optionalAbsent ();

	}

	// static data

	DurationFormat[] durationFormats =
		new DurationFormat [] {

		new ZeroDurationFormat (),

		new SimpleDurationFormat (
			31556736000l,
			"year",
			"years"),

		new SimpleDurationFormat (
			2629728000l,
			"month",
			"months"),

		new SimpleDurationFormat (
			86400000l,
			"day",
			"days",
			"d"),

		new SimpleDurationFormat (
			3600000l,
			"hour",
			"hours",
			"h"),

		new SimpleDurationFormat (
			60000l,
			"minute",
			"minutes",
			"mins",
			"min",
			"m"),

		new SimpleDurationFormat (
			1000l,
			"second",
			"seconds",
			"secs",
			"sec",
			"s"),

		new SimpleDurationFormat (
			1l,
			"millisecond",
			"milliseconds",
			"msecs",
			"msec",
			"ms")

	};

}
