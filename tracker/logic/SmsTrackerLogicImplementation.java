package wbs.sms.tracker.logic;

import static wbs.utils.etc.EnumUtils.enumInSafe;
import static wbs.utils.etc.NumberUtils.integerEqualSafe;
import static wbs.utils.time.TimeUtils.earlierThan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.joda.time.Duration;
import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

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

	// singleton dependencies

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	SmsSimpleTrackerObjectHelper smsSimpleTrackerHelper;

	@SingletonDependency
	SmsSimpleTrackerNumberObjectHelper smsSimpleTrackerNumberHelper;

	// implementation

	@Override
	public
	boolean simpleTrackerConsult (
			@NonNull Transaction parentTransaction,
			@NonNull SmsSimpleTrackerRec smsSimpleTracker,
			@NonNull NumberRec number,
			@NonNull Optional<Instant> date) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"simpleTrackerConsult");

		) {

			SmsSimpleTrackerNumberRec smsSimpleTrackerNumber =
				smsSimpleTrackerNumberHelper.find (
					transaction,
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
						transaction,
						smsSimpleTrackerNumber);

				smsSimpleTrackerNumberHelper.insert (
					transaction,
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
					transaction,
					smsSimpleTrackerNumber);

			}

			// or just return the current value

			return ! smsSimpleTrackerNumber.getBlocked ();

		}

	}

	private
	boolean simpleTrackerNumberScanAndUpdate (
			@NonNull Transaction parentTransaction,
			@NonNull SmsSimpleTrackerNumberRec trackerNumber) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"simpleTrackerNumberScanAndUpdate");

		) {

			boolean result =
				simpleTrackerScan (
					transaction,
					trackerNumber.getSmsSimpleTracker (),
					trackerNumber.getNumber ());

			trackerNumber

				.setLastScan (
					transaction.now ())

				.setBlocked (
					! result);

			return result;

		}

	}

	@Override
	public
	boolean simpleTrackerScan (
			@NonNull Transaction parentTransaction,
			@NonNull SmsSimpleTrackerRec smsSimpleTracker,
			@NonNull NumberRec number) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"simpleTrackerScan");

		) {

			// get the messages

			List <MessageRec> messages =
				smsSimpleTrackerHelper.findMessages (
					transaction,
					smsSimpleTracker,
					number);

			// delegate to simpleTrackerScanCode()

			SimpleTrackerResult result =
				simpleTrackerScanCore (
					transaction,
					messages,
					smsSimpleTracker.getFailureCountMin (),
					1000L * smsSimpleTracker.getFailureSingleSecsMin (),
					1000L * smsSimpleTracker.getFailureTotalSecsMin ());

			// and return

			return result != SimpleTrackerResult.notOk;

		}

	}

	@Override
	public
	SimpleTrackerResult simpleTrackerScanCore (
			@NonNull Transaction parentTransaction,
			@NonNull Collection <? extends MessageRec> messagesSource,
			long failureCountMin,
			long failureSingleMsMin,
			long failureTotalMsMin) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"simpleTrackerScanCore");

		) {

			// sort the messages

			List <MessageRec> messages =
				new ArrayList<> ();

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
