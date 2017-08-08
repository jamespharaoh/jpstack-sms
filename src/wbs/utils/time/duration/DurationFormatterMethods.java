package wbs.utils.time.duration;

import static wbs.utils.etc.OptionalUtils.optionalGetRequired;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.joda.time.Duration;
import org.joda.time.ReadableDuration;
import org.joda.time.ReadableInstant;

public
interface DurationFormatterMethods {

	// duration string

	String durationStringNumeric (
			ReadableDuration interval);

	default
	String durationStringNumeric (
			@NonNull ReadableInstant start,
			@NonNull ReadableInstant end) {

		return durationStringNumeric (
			new Duration (
				start,
				end));

	}

	String durationStringExact (
			ReadableDuration interval);

	default
	String durationStringExact (
			@NonNull ReadableInstant start,
			@NonNull ReadableInstant end) {

		return durationStringExact (
			new Duration (
				start,
				end));

	}

	String durationStringApproximate (
			ReadableDuration interval);

	default
	String durationStringApproximate (
			@NonNull ReadableInstant start,
			@NonNull ReadableInstant end) {

		return durationStringApproximate (
			new Duration (
				start,
				end));

	}

	// string to duration

	Optional <Duration> stringToDuration (
			String input);

	default
	Duration stringToDurationRequired (
			@NonNull String input) {

		return optionalGetRequired (
			stringToDuration (
				input));

	}

}
