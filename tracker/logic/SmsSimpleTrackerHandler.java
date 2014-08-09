package wbs.sms.tracker.logic;

import java.util.Date;

import javax.inject.Inject;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.tracker.model.SmsSimpleTrackerObjectHelper;
import wbs.sms.tracker.model.SmsSimpleTrackerRec;
import wbs.sms.tracker.model.SmsTrackerRec;

@SingletonComponent ("smsSimpleTrackerHandler")
public
class SmsSimpleTrackerHandler
	implements SmsTrackerHandler {

	@Inject
	SmsSimpleTrackerObjectHelper smsSimpleTrackerHelper;

	@Inject
	SmsTrackerLogic smsTrackerLogic;

	@Override
	public String getTypeCode () {
		return "default";
	}

	@Override
	public
	boolean canSend (
			SmsTrackerRec tracker,
			NumberRec number,
			Date timestamp) {

		SmsSimpleTrackerRec simpleTracker =
			smsSimpleTrackerHelper.find (
				tracker.getParentObjectId ());

		return smsTrackerLogic.simpleTrackerConsult (
			simpleTracker,
			number,
			timestamp);

	}

}
