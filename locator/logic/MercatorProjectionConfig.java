package wbs.sms.locator.logic;

import javax.inject.Inject;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.sms.locator.model.BiaxialEllipsoid;
import wbs.sms.locator.model.MercatorProjectionImpl;

// TODO should this be somewhere else?
@SingletonComponent ("mercatorProjectionConfig")
public
class MercatorProjectionConfig {

	@Inject
	LocatorLogic locatorLogic;

	@SingletonComponent ("ukNationalGrid")
	public
	MercatorProjectionImpl ukNationalGrid () {

		return new MercatorProjectionImpl (
			BiaxialEllipsoid.airy1830,
			0.9996012717D,
			- 2.0D,
			49.0D,
			400000.0D,
			- 100000.0D);

	}

	@SingletonComponent ("irishNationalGrid")
	public
	MercatorProjectionImpl irishNationalGrid () {

		return new MercatorProjectionImpl (
			BiaxialEllipsoid.airy1830modified,
			1.000035D,
			- 8.0D,
			locatorLogic.dmsToDeg (53, 30, 0.0D),
			200000D,
			250000D);

	}

}
