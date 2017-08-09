package wbs.sms.locator.misc;

import wbs.framework.component.annotations.NamedDependency;
import wbs.framework.component.annotations.SingletonDependency;

import wbs.sms.locator.logic.LocatorLogic;
import wbs.sms.locator.model.EastNorth;
import wbs.sms.locator.model.LongLat;
import wbs.sms.locator.model.MercatorProjection;

import junit.framework.TestCase;

public
class LocatorLogicTest
	extends TestCase {

	// singleton dependencies

	@SingletonDependency
	LocatorLogic locatorLogic;

	@SingletonDependency
	@NamedDependency
	MercatorProjection ukNationalGrid;

	// tests

	public
	void testLongLatToEastNorth () {

		LongLat longLat =
			new LongLat (
				locatorLogic.dmsToDeg (1, 43, 4.5177),
				locatorLogic.dmsToDeg (52, 39, 27.2531));

		EastNorth eastNorth =
			locatorLogic.longLatToEastNorth (
				ukNationalGrid,
				longLat);

		EastNorth expected =
			new EastNorth (
				651409.903D,
				313177.270D);

		assertTrue (
			Math.abs (
				+ eastNorth.getEasting ()
				- expected.getEasting ()
			) < 1);

		assertTrue (
			Math.abs (
				+ eastNorth.getNorthing ()
				- expected.getNorthing ()
			) < 1);

	}

	public
	void testEastNorthToLongLat () {

		EastNorth eastNorth =
			new EastNorth (
				651409.903D,
				313177.270D);

		LongLat longLat =
			locatorLogic.eastNorthToLongLat (
				ukNationalGrid,
				eastNorth);

		LongLat expected =
			new LongLat (
				locatorLogic.dmsToDeg (1, 43, 4.5177),
				locatorLogic.dmsToDeg (52, 39, 27.2531));

		assertTrue (
			Math.abs (
				+ longLat.longitude ()
				- expected.longitude ()
			) < 0.0001);

		assertTrue (
			Math.abs (
				+ longLat.latitude ()
				- expected.latitude ()
			) < 0.0001);

	}

	public
	void testDistanceMetresLongLatLongLat () {

		LongLat longLat1 =
			new LongLat (
				- locatorLogic.dmsToDeg (1, 50, 40.0D),
				+ locatorLogic.dmsToDeg (53, 9, 2.0D));

		LongLat longLat2 =
			new LongLat (
				- locatorLogic.dmsToDeg (0, 8, 33.0D),
				+ locatorLogic.dmsToDeg (52, 12, 19.0D));

		double distance =
			locatorLogic.distanceMetres (
				longLat1,
				longLat2);

		double expected = 155927.727D;

		assertTrue (
			Math.abs (
				+ distance
				- expected
			) < 0.001);

	}

}
