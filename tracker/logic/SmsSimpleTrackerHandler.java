package wbs.sms.tracker.logic;

import javax.inject.Inject;

import lombok.NonNull;

import org.joda.time.Instant;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.tracker.model.SmsSimpleTrackerObjectHelper;
import wbs.sms.tracker.model.SmsSimpleTrackerRec;
import wbs.sms.tracker.model.SmsTrackerRec;

@SingletonComponent ("smsSimpleTrackerHandler")
public
class SmsSimpleTrackerHandler
	implements SmsTrackerHandler {

	// dependencies

	@Inject
	SmsSimpleTrackerObjectHelper smsSimpleTrackerHelper;

	@Inject
	SmsTrackerLogic smsTrackerLogic;

	// details

	@Override
	public
	String getTypeCode () {
		return "default";
	}

	// implementation

	@Override
	public
	boolean canSend (
			@NonNull SmsTrackerRec tracker,
			@NonNull NumberRec number,
			@NonNull Optional<Instant> timestamp) {

		SmsSimpleTrackerRec simpleTracker =
			smsSimpleTrackerHelper.findRequired (
				tracker.getParentId ());

		return smsTrackerLogic.simpleTrackerConsult (
			simpleTracker,
			number,
			timestamp);

	}

}
