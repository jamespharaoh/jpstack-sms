package wbs.sms.locator.model;

import lombok.Getter;
import lombok.NonNull;

public
class MercatorProjectionImplementation
	implements MercatorProjection {

	BiaxialEllipsoidImplementation biaxialEllipsoid;

	@Getter
	Double scaleFactor;

	@Getter
	Double originLongitude;

	@Getter
	Double originLatitude;

	@Getter
	Double originEasting;

	@Getter
	Double originNorthing;

	public
	MercatorProjectionImplementation (
			@NonNull BiaxialEllipsoidImplementation newBiaxialEllipsoid,
			@NonNull Double newScaleFactor,
			@NonNull Double newOriginLongitude,
			@NonNull Double newOriginLatitude,
			@NonNull Double newOriginEasting,
			@NonNull Double newOriginNorthing) {

		if (newOriginLongitude < -180
				|| newOriginLongitude > 180
				|| newOriginLatitude < -180
				|| newOriginLatitude > 180)
			throw new IllegalArgumentException ();

		biaxialEllipsoid =
			newBiaxialEllipsoid;

		scaleFactor =
			newScaleFactor;

		originLongitude =
			newOriginLongitude;

		originLatitude =
			newOriginLatitude;

		originEasting =
			newOriginEasting;

		originNorthing =
			newOriginNorthing;

	}

	@Override
	public
	BiaxialEllipsoidImplementation getBiaxialEllipsoid () {
		return biaxialEllipsoid;
	}

}
