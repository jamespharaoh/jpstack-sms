package wbs.sms.tracker.logic;

import java.util.Collection;

import com.google.common.base.Optional;

import org.joda.time.Instant;

import wbs.framework.database.Transaction;

import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.tracker.model.SmsSimpleTrackerRec;

public
interface SmsTrackerLogic {

	boolean simpleTrackerConsult (
			Transaction parentTransaction,
			SmsSimpleTrackerRec tracker,
			NumberRec number,
			Optional <Instant> date);

	boolean simpleTrackerScan (
			Transaction parentTransaction,
			SmsSimpleTrackerRec tracker,
			NumberRec number);

	SimpleTrackerResult simpleTrackerScanCore (
			Transaction parentTransaction,
			Collection <? extends MessageRec> messagesSource,
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
