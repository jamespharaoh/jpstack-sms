package wbs.sms.locator.model;

import static wbs.framework.utils.etc.Misc.notEqual;
import static wbs.framework.utils.etc.Misc.optionalRequired;
import static wbs.framework.utils.etc.Misc.split;

import java.util.List;

import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;

import com.google.common.base.Optional;

@Accessors (fluent = true)
@Value
public
class LongLat {

	Double longitude;
	Double latitude;

	public static
	Optional<LongLat> parse (
			@NonNull String string) {

		List<String> parts =
			split (
				string,
				",");

		if (
			notEqual (
				parts.size (),
				2)
		) {
			return Optional.absent ();
		}

		Double longitude;
		Double latitude;

		try {

			longitude =
				Double.parseDouble (
					parts.get (0));

			latitude =
				Double.parseDouble (
					parts.get (1));

		} catch (NumberFormatException exception) {
			return Optional.absent ();
		}

		return Optional.of (
			new LongLat (
				longitude,
				latitude));

	}

	public static
	LongLat parseRequired (
			@NonNull String string) {

		return optionalRequired (
			parse (
				string));

	}

}
