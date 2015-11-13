package wbs.sms.tracker.logic;

import static wbs.framework.utils.etc.Misc.in;
import static wbs.framework.utils.etc.Misc.instantToDate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.inject.Inject;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.tracker.model.SmsSimpleTrackerNumberObjectHelper;
import wbs.sms.tracker.model.SmsSimpleTrackerNumberRec;
import wbs.sms.tracker.model.SmsSimpleTrackerObjectHelper;
import wbs.sms.tracker.model.SmsSimpleTrackerRec;

@SingletonComponent ("smsTrackerLogic")
public
class SmsTrackerLogicImpl
	implements SmsTrackerLogic {

	// dependencies

	@Inject
	Database database;

	@Inject
	SmsSimpleTrackerObjectHelper smsSimpleTrackerHelper;

	@Inject
	SmsSimpleTrackerNumberObjectHelper smsSimpleTrackerNumberHelper;

	// implementation

	@Override
	public
	boolean simpleTrackerConsult (
			SmsSimpleTrackerRec smsSimpleTracker,
			NumberRec number,
			Date date) {

		SmsSimpleTrackerNumberRec smsSimpleTrackerNumber =
			smsSimpleTrackerNumberHelper.find (
				smsSimpleTracker,
				number);

		// create a new tracker number

		if (smsSimpleTrackerNumber == null) {

			smsSimpleTrackerNumber =
				smsSimpleTrackerNumberHelper.createInstance ()

				.setSmsSimpleTracker (
					smsSimpleTracker)

				.setNumber (
					number)

				.setBlocked (
					false);

			boolean result =
				simpleTrackerNumberScanAndUpdate (
					smsSimpleTrackerNumber);

			smsSimpleTrackerNumberHelper.insert (
				smsSimpleTrackerNumber);

			return result;

		}

		// if we have an existing blocked just return that

		if (smsSimpleTrackerNumber.getBlocked ())
			return false;

		// check if the result has expired

		Calendar cal =
			new GregorianCalendar ();

		cal.setTime (
			smsSimpleTrackerNumber.getLastScan ());

		cal.add (
			Calendar.SECOND,
			smsSimpleTracker.getSinceScanSecsMax ());

		if (
			cal.getTime ().getTime ()
				< System.currentTimeMillis ()
		) {

			return simpleTrackerNumberScanAndUpdate (
				smsSimpleTrackerNumber);

		}

		// or just return the current value

		return ! smsSimpleTrackerNumber.getBlocked ();

	}

	/**
	 * Performs an immediate scan on the given tracker number and updates it
	 * accordingley.
	 *
	 * @param trackerNumber
	 *            The tracker number to update.
	 * @return True if the number should be sent to.
	 */
	private
	boolean simpleTrackerNumberScanAndUpdate (
			SmsSimpleTrackerNumberRec trackerNumber) {

		Transaction transaction =
			database.currentTransaction ();

		boolean result =
			simpleTrackerScan (
				trackerNumber.getSmsSimpleTracker (),
				trackerNumber.getNumber ());

		trackerNumber

			.setLastScan (
				instantToDate (
					transaction.now ()))

			.setBlocked (
				! result);

		return result;

	}

	@Override
	public
	boolean simpleTrackerScan (
			SmsSimpleTrackerRec smsSimpleTracker,
			NumberRec number) {

		// get the messages

		List<MessageRec> messages =
			smsSimpleTrackerHelper.findMessages (
				smsSimpleTracker,
				number);

		// delegate to simpleTrackerScanCode()

		SimpleTrackerResult result =
			simpleTrackerScanCore (
				messages,
				smsSimpleTracker.getFailureCountMin (),
				1000L * smsSimpleTracker.getFailureSingleSecsMin (),
				1000L * smsSimpleTracker.getFailureTotalSecsMin ());

		// and return

		return result != SimpleTrackerResult.notOk;

	}

	@Override
	public
	SimpleTrackerResult simpleTrackerScanCore (
			Collection<? extends MessageRec> messagesSource,
			int failureCountMin,
			long failureSingleMsMin,
			long failureTotalMsMin) {

		// sort the messages

		List<MessageRec> messages =
			new ArrayList<MessageRec> ();

		messages.addAll (
			messagesSource);

		Collections.sort (
			messages);

		long lastTime = 0;
		long lastCountedTime = 0;
		long firstTime = 0;

		int numFound = 0;

		for (
			MessageRec message
				: messages
		) {

			long thisTime =
				message.getCreatedTime ().getTime ();

			// if we see a message delivered we stop straight away

			if (
				simpleTrackerStatusIsPositive (
					message.getStatus ())
			) {
				return SimpleTrackerResult.ok;
			}

			// then we are only interested in undelivereds

			if (
				! simpleTrackerStatusIsNegative (
					message.getStatus ())
			) {
				continue;
			}

			if (

				// if its the first one we always pick it up

				lastTime == 0

			) {

				lastCountedTime =
				firstTime =
					thisTime;

				numFound ++;

			} else if (

				// otherwise we only care if it is far enough back from the last
				// one we counted

				thisTime + failureSingleMsMin
					< lastCountedTime

			) {

				lastCountedTime =
					thisTime;

				numFound ++;

			}

			// if we have counted enough return affirmative

			if (

				numFound
					>= failureCountMin

				&& thisTime + failureTotalMsMin
					< firstTime

			) {

				return SimpleTrackerResult.notOk;

			}

			lastTime = thisTime;

		}

		// if we make it here there can't have been enough messages to decide

		return SimpleTrackerResult.okSoFar;

	}

	@Override
	public
	boolean simpleTrackerStatusIsPositive (
			MessageStatus status) {

		return in (
			status,
			MessageStatus.delivered);

	}

	@Override
	public
	boolean simpleTrackerStatusIsNegative (
			MessageStatus status) {

		return in (
			status,
			MessageStatus.reportTimedOut,
			MessageStatus.undelivered);

	}

}
