package wbs.sms.tracker.logic;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import lombok.NonNull;

import org.joda.time.Instant;

import com.google.common.base.Optional;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.tracker.model.SmsTrackerRec;

@SingletonComponent ("smsTrackerManager")
public
class SmsTrackerManagerImplementation
	implements SmsTrackerManager {

	// dependencies

	@Inject
	Database database;

	// collection dependencies

	@Inject
	Map<String,SmsTrackerHandler> trackerHandlersByName;

	// state

	Map<String,SmsTrackerHandler> trackerHandlersByTypeCode;

	// life cycle

	@PostConstruct
	public
	void afterPropertiesSet () {

		trackerHandlersByTypeCode =
			new HashMap<String,SmsTrackerHandler> ();

		for (
			SmsTrackerHandler trackerHandler
				: trackerHandlersByName.values ()
		) {

			trackerHandlersByTypeCode.put (
				trackerHandler.getTypeCode (),
				trackerHandler);

		}

	}

	// implementation

	@Override
	public
	boolean canSend (
			@NonNull SmsTrackerRec tracker,
			@NonNull NumberRec number,
			@NonNull Optional<Instant> timestamp) {

		SmsTrackerHandler handler =
			trackerHandlersByTypeCode.get (
				tracker.getSmsTrackerType ().getCode ());

		return handler.canSend (
			tracker,
			number,
			timestamp);

	}

}