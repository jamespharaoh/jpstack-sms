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

@SingletonComponent ("ukNationalGrid")
public
class UkNationalGrid
	implements ComponentFactory <MercatorProjectionImplementation> {

	// singleton dependencies

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
					"ukNationalGrid");

		) {

			return new MercatorProjectionImplementation (
				BiaxialEllipsoid.airy1830,
				0.9996012717D,
				- 2.0D,
				49.0D,
				400000.0D,
				- 100000.0D);

		}

	}

}
