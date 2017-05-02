package wbs.sms.tracker.logic;

import com.google.common.base.Optional;

import org.joda.time.Instant;

import wbs.framework.database.Transaction;

import wbs.sms.number.core.model.NumberRec;
import wbs.sms.tracker.model.SmsTrackerRec;

public
interface SmsTrackerHandler {

	String getTypeCode ();

	boolean canSend (
			Transaction parentTransaction,
			SmsTrackerRec tracker,
			NumberRec number,
			Optional <Instant> timestamp);

}
