package wbs.utils.time.duration;

import com.google.common.base.Optional;

import org.joda.time.Duration;
import org.joda.time.ReadableDuration;

public
interface DurationFormat {

	Optional <Duration> match (
			String input);

	Optional <String> textify (
			ReadableDuration input);

}
