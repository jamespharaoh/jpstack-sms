package wbs.sms.locator.model;

import static wbs.utils.collection.CollectionUtils.collectionDoesNotHaveTwoElements;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.string.StringUtils.stringSplitComma;

import java.util.List;

import com.google.common.base.Optional;

import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
@Value
public
class LongLat {

	Double longitude;
	Double latitude;

	public static
	Optional <LongLat> parse (
			@NonNull String string) {

		List <String> parts =
			stringSplitComma (
				string);

		if (
			collectionDoesNotHaveTwoElements (
				parts)
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

		return optionalGetRequired (
			parse (
				string));

	}

}
