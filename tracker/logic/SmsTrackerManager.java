package wbs.sms.tracker.logic;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.tracker.model.SmsTrackerRec;

@SingletonComponent ("smsTrackerManager")
public
class SmsTrackerManager {

	@Inject
	Database database;

	@Inject
	Map<String,SmsTrackerHandler> trackerHandlersByName;

	Map<String,SmsTrackerHandler> trackerHandlersByTypeCode;

	public
	boolean canSend (
			SmsTrackerRec tracker,
			NumberRec number,
			Date timestamp) {

		SmsTrackerHandler handler =
			trackerHandlersByTypeCode.get (
				tracker.getSmsTrackerType ().getCode ());

		return handler.canSend (
			tracker,
			number,
			timestamp);

	}

	@PostConstruct
	public
	void afterPropertiesSet () {

		trackerHandlersByTypeCode =
			new HashMap<String,SmsTrackerHandler> ();

		for (SmsTrackerHandler trackerHandler
			: trackerHandlersByName.values ()) {

			trackerHandlersByTypeCode.put (
				trackerHandler.getTypeCode (),
				trackerHandler);
		}

	}

}