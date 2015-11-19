package wbs.sms.tracker.logic;

import org.joda.time.Instant;

import com.google.common.base.Optional;

import wbs.sms.number.core.model.NumberRec;
import wbs.sms.tracker.model.SmsTrackerRec;

public
interface SmsTrackerManager {

	boolean canSend (
			SmsTrackerRec tracker,
			NumberRec number,
			Optional<Instant> timestamp);

}
