package wbs.sms.tracker.logic;

import org.joda.time.Duration;
import org.joda.time.Instant;

import wbs.framework.component.annotations.SingletonDependency;

import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;

import junit.framework.TestCase;

public
class SmsUtilsTrackerTest
	extends TestCase {

	// singleton dependencies

	@SingletonDependency
	MessageObjectHelper messageHelper;

	// implementation

	MessageRec createMessage (
			int days,
			MessageStatus status) {

		MessageRec message =
			messageHelper.createInstance ();

		message.setCreatedTime (
			Instant.now ().minus (
				Duration.standardDays (
					days)));

		message.setStatus (
			status);

		return message;

	}

	static
	long
		ms = 1,
		second = ms * 1000,
		minute = second * 60,
		hour = minute * 60,
		day = hour * 24;

	/*
	public
	void testSimpleTrackerScanCore () {

		SmsLogic smsUtils =
			new SmsLogicImplementation ();

		SmsLogicImplementation.SimpleTrackerResult result;

		// notOk

		result =
			smsUtils.simpleTrackerScanCore (
				ImmutableList.<MessageRec>of (
					msg (1, MessageStatus.undelivered),
					msg (30, MessageStatus.undelivered),
					msg (31, MessageStatus.undelivered),
					msg (60, MessageStatus.undelivered)),
				3,
				7 * day,
				30 * day);

		assertEquals (
			SmsLogic.SimpleTrackerResult.notOk,
			result);

		// ok: a message is successful

		result =
			smsUtils.simpleTrackerScanCore (
				ImmutableList.<MessageRec>of (
					msg (1, MessageStatus.undelivered),
					msg (30, MessageStatus.undelivered),
					msg (31, MessageStatus.delivered),
					msg (60, MessageStatus.undelivered)),
					3,
					7 * day,
					30 * day);

		assertEquals (
			SmsLogic.SimpleTrackerResult.ok,
			result);

		// okSoFar: there aren't enough messages

		result =
			smsUtils.simpleTrackerScanCore (
				ImmutableList.<MessageRec>of (
					msg (1, MessageStatus.undelivered),
					msg (30, MessageStatus.undelivered),
					msg (31, MessageStatus.undelivered),
					msg (60, MessageStatus.undelivered)),
					4,
					7 * day,
					30 * day);

		assertEquals (
			SmsLogic.SimpleTrackerResult.okSoFar,
			result);

		// notOk: the two close messages are counted separate now

		result =
			smsUtils.simpleTrackerScanCore (
				ImmutableList.<MessageRec>of (
					msg (1, MessageStatus.undelivered),
					msg (30, MessageStatus.undelivered),
					msg (31, MessageStatus.undelivered),
					msg (60, MessageStatus.undelivered)),
					4,
					6 * hour,
					30 * day);

		assertEquals (
			SmsLogic.SimpleTrackerResult.notOk,
			result);

		// notOk: this one includes a "submitted" as well and some "delivered" a
		// while ago

		result =
			smsUtils.simpleTrackerScanCore (
				ImmutableList.<MessageRec>of (
					msg (6, MessageStatus.undelivered),
					msg (88, MessageStatus.submitted),
					msg (108, MessageStatus.undelivered),
					msg (282, MessageStatus.undelivered),
					msg (310, MessageStatus.undelivered),
					msg (319, MessageStatus.undelivered),
					msg (322, MessageStatus.undelivered),
					msg (338, MessageStatus.delivered),
					msg (366, MessageStatus.delivered)),
					3,
					7 * day,
					90 * day);

		assertEquals (
			SmsLogic.SimpleTrackerResult.notOk,
			result);
	}
	*/

}
