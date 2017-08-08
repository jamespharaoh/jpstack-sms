package wbs.utils.time.core;

import static wbs.utils.collection.MapUtils.mapItemForKeyRequired;
import static wbs.utils.collection.MapUtils.mapWithDerivedKey;

import java.util.List;
import java.util.Map;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@SingletonComponent ("timeFormatterManager")
public
class TimeFormatterManager {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	List <TimeFormatter> timeFormatters;

	// state

	Map <String, TimeFormatter> timeFormattersByName;

	// life cycle

	@NormalLifecycleSetup
	public
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setup");

		) {

			timeFormattersByName =
				mapWithDerivedKey (
					timeFormatters,
					TimeFormatter::name);

		}

	}

	// public implementation

	public
	TimeFormatter forNameRequired (
			@NonNull String name) {

		return mapItemForKeyRequired (
			timeFormattersByName,
			name);

	}

}
