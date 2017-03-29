package wbs.sms.tracker.logic;

import static wbs.utils.collection.MapUtils.mapWithDerivedKey;

import java.util.Map;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.sms.number.core.model.NumberRec;
import wbs.sms.tracker.model.SmsTrackerRec;

@SingletonComponent ("smsTrackerManager")
public
class SmsTrackerManagerImplementation
	implements SmsTrackerManager {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	// collection dependencies

	@SingletonDependency
	Map <String, SmsTrackerHandler> trackerHandlersByName;

	// state

	Map <String, SmsTrackerHandler> trackerHandlersByTypeCode;

	// life cycle

	@NormalLifecycleSetup
	public
	void afterPropertiesSet () {

		trackerHandlersByTypeCode =
			mapWithDerivedKey (
				trackerHandlersByName.values (),
				SmsTrackerHandler::getTypeCode);

	}

	// implementation

	@Override
	public
	boolean canSend (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull SmsTrackerRec tracker,
			@NonNull NumberRec number,
			@NonNull Optional <Instant> timestamp) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"canSend");

		SmsTrackerHandler handler =
			trackerHandlersByTypeCode.get (
				tracker.getSmsTrackerType ().getCode ());

		return handler.canSend (
			taskLogger,
			tracker,
			number,
			timestamp);

	}

}