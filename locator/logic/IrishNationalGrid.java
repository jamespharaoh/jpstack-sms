package wbs.sms.locator.logic;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.sms.locator.model.BiaxialEllipsoid;
import wbs.sms.locator.model.MercatorProjectionImplementation;

@SingletonComponent ("irishNationalGrid")
public
class IrishNationalGrid
	implements ComponentFactory <MercatorProjectionImplementation> {

	// dependencies

	@SingletonDependency
	LocatorLogic locatorLogic;

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	MercatorProjectionImplementation makeComponent (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"makeComponent");

		) {

			return new MercatorProjectionImplementation (
				BiaxialEllipsoid.airy1830modified,
				1.000035D,
				- 8.0D,
				locatorLogic.dmsToDeg (53, 30, 0.0D),
				200000D,
				250000D);

		}

	}

}
