package wbs.sms.tracker.logic;

import static wbs.framework.utils.etc.EnumUtils.enumInSafe;
import static wbs.framework.utils.etc.NumberUtils.integerEqualSafe;
import static wbs.framework.utils.etc.TimeUtils.earlierThan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.joda.time.Duration;
import org.joda.time.Instant;

import com.google.common.base.Optional;

import lombok.NonNull;
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
class SmsTrackerLogicImplementation
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
			@NonNull SmsSimpleTrackerRec smsSimpleTracker,
			@NonNull NumberRec number,
			@NonNull Optional<Instant> date) {

		Transaction transaction =
			database.currentTransaction ();

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

		Instant expiryTime =
			smsSimpleTrackerNumber.getLastScan ().plus (
				Duration.standardSeconds (
					smsSimpleTracker.getSinceScanSecsMax ()));

		if (
			earlierThan (
				expiryTime,
				transaction.now ())
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
			@NonNull SmsSimpleTrackerNumberRec trackerNumber) {

		Transaction transaction =
			database.currentTransaction ();

		boolean result =
			simpleTrackerScan (
				trackerNumber.getSmsSimpleTracker (),
				trackerNumber.getNumber ());

		trackerNumber

			.setLastScan (
				transaction.now ())

			.setBlocked (
				! result);

		return result;

	}

	@Override
	public
	boolean simpleTrackerScan (
			@NonNull SmsSimpleTrackerRec smsSimpleTracker,
			@NonNull NumberRec number) {

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
			@NonNull Collection<? extends MessageRec> messagesSource,
			long failureCountMin,
			long failureSingleMsMin,
			long failureTotalMsMin) {

		// sort the messages

		List<MessageRec> messages =
			new ArrayList<MessageRec> ();

		messages.addAll (
			messagesSource);

		Collections.sort (
			messages);

		Instant lastTime =
			new Instant (0);

		Instant lastCountedTime =
			new Instant (0);

		Instant firstTime =
			new Instant (0);

		int numFound = 0;

		for (
			MessageRec message
				: messages
		) {

			Instant thisTime =
				message.getCreatedTime ();

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

				integerEqualSafe (
					lastTime.getMillis (),
					0l)

			) {

				lastCountedTime =
					thisTime;

				firstTime =
					thisTime;

				numFound ++;

			} else if (

				// otherwise we only care if it is far enough back from the last
				// one we counted

				earlierThan (
					thisTime.plus (
						failureSingleMsMin),
					lastCountedTime)

			) {

				lastCountedTime =
					thisTime;

				numFound ++;

			}

			// if we have counted enough return affirmative

			if (

				numFound
					>= failureCountMin

				&& earlierThan (
					thisTime.plus (
						failureTotalMsMin),
					firstTime)

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
			@NonNull MessageStatus status) {

		return enumInSafe (
			status,
			MessageStatus.delivered);

	}

	@Override
	public
	boolean simpleTrackerStatusIsNegative (
			@NonNull MessageStatus status) {

		return enumInSafe (
			status,
			MessageStatus.reportTimedOut,
			MessageStatus.undelivered);

	}

}
