package wbs.sms.locator.logic;

import wbs.sms.locator.model.BiaxialEllipsoid;
import wbs.sms.locator.model.EastNorth;
import wbs.sms.locator.model.LongLat;
import wbs.sms.locator.model.MercatorProjection;

public
interface LocatorLogic {

	EastNorth longLatToEastNorth (
			MercatorProjection mercatorProjection,
			LongLat longLat);

	LongLat eastNorthToLongLat (
			MercatorProjection mercatorProjection,
			EastNorth eastNorth);

	double distanceMetres (
			LongLat longLat1,
			LongLat longLat2,
			BiaxialEllipsoid biaxialEllipsoid);

	double distanceMetres (
			LongLat longLat1,
			LongLat longLat2);

	double distanceMiles (
			LongLat longLat1,
			LongLat longLat2,
			BiaxialEllipsoid biaxialEllipsoid);

	double distanceMiles (
			LongLat longLat1,
			LongLat longLat2);

	double dmsToRad (
			int degrees,
			int minutes,
			double seconds);

	double dmsToDeg (
			int degrees,
			int minutes,
			double seconds);

	double degToRad (
			double degrees);

	double radToDeg (
			double radians);

}