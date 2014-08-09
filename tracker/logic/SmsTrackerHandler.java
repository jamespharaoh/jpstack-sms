package wbs.sms.tracker.logic;

import java.util.Date;

import wbs.sms.number.core.model.NumberRec;
import wbs.sms.tracker.model.SmsTrackerRec;

public interface SmsTrackerHandler {

	String getTypeCode ();

	boolean canSend (
		SmsTrackerRec tracker,
		NumberRec number,
		Date timestamp);
}
