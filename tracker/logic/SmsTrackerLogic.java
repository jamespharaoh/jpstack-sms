package wbs.sms.tracker.logic;

import java.util.Collection;

import com.google.common.base.Optional;

import org.joda.time.Instant;

import wbs.framework.logging.TaskLogger;

import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.tracker.model.SmsSimpleTrackerRec;

public
interface SmsTrackerLogic {

	/**
	 * The normal way to use a simple tracker, returns any cached value or does
	 * a scan if the data is out of date or unavailable, saving the result for
	 * future consultations.
	 *
	 * @param tracker
	 *            The simple tracker to consult.
	 * @param number
	 *            The number to check.
	 * @param date
	 *            The date the number was obtained (TODO: ignored).
	 * @return True if the number should be sent to.
	 */
	boolean simpleTrackerConsult (
			TaskLogger parentTaskLogger,
			SmsSimpleTrackerRec tracker,
			NumberRec number,
			Optional <Instant> date);

	/**
	 * This entirely passive function does a scan for a given tracker and number
	 * and finds out if it should be blocked or not.
	 *
	 * @param tracker
	 *            The tracker to get the scan parameters from.
	 * @param number
	 *            The number to scan.
	 * @return True if the number should be sent to.
	 */
	boolean simpleTrackerScan (
			SmsSimpleTrackerRec tracker,
			NumberRec number);

	/**
	 * This entirely passive function does a scan for a some given tracker
	 * parameters and messages finds out if it should be blocked or not.
	 *
	 * @param messages
	 *            The messages to scan.
	 * @param failureCountMin
	 *            The minimum number of consecutive failures to look for.
	 * @param failureSingleSecsMin
	 *            The minimum amount of time between failures for them to be
	 *            counted separately.
	 * @param failureTotalSecsMin
	 *            The minimum amount of time between the most and least recent
	 *            failures.
	 * @return The appropriate SimpleTrackerResult.
	 */
	SimpleTrackerResult simpleTrackerScanCore (
			Collection<? extends MessageRec> messagesSource,
			long failureCountMin,
			long failureSingleMsMin,
			long failureTotalMsMin);

	boolean simpleTrackerStatusIsPositive (
			MessageStatus status);

	boolean simpleTrackerStatusIsNegative (
			MessageStatus status);

	/**
	 * Represents the result of a simple tracker scan:
	 * - ok: the number is definitely ok
	 * - okSoFar: the number is ok but more messages might change the result
	 * - notOk: the number is NOT ok
	 */
	public static
	enum SimpleTrackerResult {
		ok,
		okSoFar,
		notOk
	}

}
