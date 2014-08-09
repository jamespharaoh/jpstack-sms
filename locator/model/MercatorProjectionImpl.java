package wbs.sms.locator.model;

import lombok.Getter;

public
class MercatorProjectionImpl
	implements MercatorProjection {

	BiaxialEllipsoidImpl biaxialEllipsoid;

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
	MercatorProjectionImpl (
			BiaxialEllipsoidImpl newBiaxialEllipsoid,
			Double newScaleFactor,
			Double newOriginLongitude,
			Double newOriginLatitude,
			Double newOriginEasting,
			Double newOriginNorthing) {

		if (newBiaxialEllipsoid == null)
			throw new NullPointerException ();

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
	BiaxialEllipsoidImpl getBiaxialEllipsoid () {
		return biaxialEllipsoid;
	}

}
