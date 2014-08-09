package wbs.sms.locator.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public
class LongLat {

	@Getter @Setter
	Double longitude;

	@Getter @Setter
	Double latitude;

	public
	LongLat () {

		this (
			null,
			null);

	}

	public
	LongLat (
			Double newLongitude,
			Double newLatitude) {

		longitude =
			newLongitude;

		latitude =
			newLatitude;

	}

}
