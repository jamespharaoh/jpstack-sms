package wbs.sms.locator.logic;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.sms.locator.model.BiaxialEllipsoid;
import wbs.sms.locator.model.MercatorProjectionImplementation;

// TODO should this be somewhere else?
@SingletonComponent ("mercatorProjectionComponents")
public
class MercatorProjectionComponents {

	// dependencies

	@SingletonDependency
	LocatorLogic locatorLogic;

	// components

	@SingletonComponent ("ukNationalGrid")
	public
	MercatorProjectionImplementation ukNationalGrid () {

		return new MercatorProjectionImplementation (
			BiaxialEllipsoid.airy1830,
			0.9996012717D,
			- 2.0D,
			49.0D,
			400000.0D,
			- 100000.0D);

	}

	@SingletonComponent ("irishNationalGrid")
	public
	MercatorProjectionImplementation irishNationalGrid () {

		return new MercatorProjectionImplementation (
			BiaxialEllipsoid.airy1830modified,
			1.000035D,
			- 8.0D,
			locatorLogic.dmsToDeg (53, 30, 0.0D),
			200000D,
			250000D);

	}

}
