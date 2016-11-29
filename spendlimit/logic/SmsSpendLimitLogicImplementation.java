package wbs.sms.spendlimit.logic;

import static wbs.utils.etc.EnumUtils.enumNotEqualSafe;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.NumberUtils.equalToZero;
import static wbs.utils.etc.NumberUtils.moreThan;
import static wbs.utils.etc.NumberUtils.notLessThan;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.time.TimeUtils.notEarlierThan;

import java.util.List;

import javax.inject.Provider;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;

import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.logic.SmsMessageSender;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.spendlimit.model.SmsSpendLimiterNumberDayObjectHelper;
import wbs.sms.spendlimit.model.SmsSpendLimiterNumberDayRec;
import wbs.sms.spendlimit.model.SmsSpendLimiterNumberObjectHelper;
import wbs.sms.spendlimit.model.SmsSpendLimiterNumberRec;
import wbs.sms.spendlimit.model.SmsSpendLimiterRec;

@SingletonComponent ("smsSpendLimitLogic")
public
class SmsSpendLimitLogicImplementation
	implements SmsSpendLimitLogic {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	SmsSpendLimiterNumberDayObjectHelper smsSpendLimiterNumberDayHelper;

	@SingletonDependency
	SmsSpendLimiterNumberObjectHelper smsSpendLimiterNumberHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider <SmsMessageSender> smsMessageSenderProvider;

	// public implementation

	@Override
	public
	Optional <Long> spendCheck (
			@NonNull SmsSpendLimiterRec spendLimiter,
			@NonNull NumberRec number) {

		if (
			isNull (
				spendLimiter.getDailySpendLimitAmount ())
		) {
			return optionalAbsent ();
		}

		SmsSpendLimiterNumberRec spendLimiterNumber =
			smsSpendLimiterNumberHelper.findOrCreate (
				spendLimiter,
				number);

		SmsSpendLimiterNumberDayRec spendLimiterNumberDay =
			numberToday (
				spendLimiterNumber);

		return optionalOf (
			+ spendLimiter.getDailySpendLimitAmount ()
			- spendLimiterNumberDay.getTotalSpent ());

	}

	@Override
	public
	void spend (
			@NonNull SmsSpendLimiterRec spendLimiter,
			@NonNull NumberRec number,
			@NonNull List <MessageRec> spendMessages,
			@NonNull Long threadId,
			@NonNull String originator) {

		SmsSpendLimiterNumberRec spendLimiterNumber =
			smsSpendLimiterNumberHelper.findOrCreate (
				spendLimiter,
				number);

		SmsSpendLimiterNumberDayRec spendLimiterNumberDay =
			numberToday (
				spendLimiterNumber);

		// sanity check

		spendMessages.forEach (
			message -> {

			if (

				enumNotEqualSafe (
					message.getDirection (),
					MessageDirection.out)

				|| equalToZero (
					message.getCharge ())

			) {
				throw new IllegalArgumentException ();
			}

		});

		// check daily spend limit

		Long amountToSpend =
			spendMessages.stream ()

			.mapToLong (
				MessageRec::getCharge)

			.sum ();

		Optional <Long> spendAvailable =
			spendCheck (
				spendLimiter,
				number);

		if (

			optionalIsPresent (
				spendAvailable)

			&& moreThan (
				amountToSpend,
				spendAvailable.get ())

		) {
			throw new RuntimeException ();
		}

		// increase counters

		spendLimiterNumber

			.setAdviceSpent (
				+ spendLimiterNumber.getAdviceSpent ()
				+ amountToSpend)

			.setLastDailySpend (
				+ spendLimiterNumber.getLastDailySpend ()
				+ amountToSpend)

			.setTotalSpent (
				+ spendLimiterNumber.getTotalSpent ()
				+ amountToSpend);

		spendLimiterNumber.getSpendMessages ().addAll (
				spendMessages);

		spendLimiterNumberDay

			.setAdviceSpent (
				+ spendLimiterNumberDay.getAdviceSpent ()
				+ amountToSpend)

			.setTotalSpent (
				+ spendLimiterNumberDay.getTotalSpent ()
				+ amountToSpend);

		spendLimiterNumberDay.getSpendMessages ().addAll (
				spendMessages);

		// check daily limit

		if (

			isNotNull (
				spendLimiter.getDailySpendLimitAmount ())

			&& notLessThan (
				spendLimiterNumberDay.getTotalSpent (),
				spendLimiter.getDailySpendLimitAmount ())

		) {

			MessageRec limitMessage =
				smsMessageSenderProvider.get ()

				.threadId (
					threadId)

				.number (
					number)

				.messageText (
					spendLimiter.getDailySpendLimitMessage ())

				.numFrom (
					originator)

				.routerResolve (
					spendLimiter.getRouter ())

				.serviceLookup (
					spendLimiter,
					"daily_limit")

				.send ();

			spendLimiterNumber

				.setAdviceSpent (
					0l);

			spendLimiterNumberDay

				.setAdviceSpent (
					0l)

				.setLimitSent (
					true)

				.setLimitMessage (
					limitMessage);

		}

		// check daily advice

		if (

			isNotNull (
				spendLimiter.getDailySpendAdviceAmount ())

			&& notLessThan (
				spendLimiterNumberDay.getAdviceSpent (),
				spendLimiter.getDailySpendAdviceAmount ())

		) {

			MessageRec adviceMessage =
				smsMessageSenderProvider.get ()

				.threadId (
					threadId)

				.number (
					number)

				.messageText (
					spendLimiter.getDailySpendAdviceMessage ())

				.numFrom (
					originator)

				.routerResolve (
					spendLimiter.getRouter ())

				.serviceLookup (
					spendLimiter,
					"daily_advice")

				.send ();

			spendLimiterNumber

				.setAdviceSpent (
					0l);

			spendLimiterNumberDay

				.setAdviceSpent (
					0l)

				.getAdviceMessages ().add (
					adviceMessage);

		}

		// check ongoing advice

		if (

			isNotNull (
				spendLimiter.getOngoingSpendAdviceAmount ())

			&& notLessThan (
				spendLimiterNumber.getAdviceSpent (),
				spendLimiter.getOngoingSpendAdviceAmount ())

		) {

			MessageRec adviceMessage =
				smsMessageSenderProvider.get ()

				.threadId (
					threadId)

				.number (
					number)

				.messageText (
					spendLimiter.getOngoingSpendAdviceMessage ())

				.numFrom (
					originator)

				.routerResolve (
					spendLimiter.getRouter ())

				.serviceLookup (
					spendLimiter,
					"ongoing_advice")

				.send ();

			spendLimiterNumber

				.setAdviceSpent (
					0l)

				.getAdviceMessages ().add (
					adviceMessage);

			spendLimiterNumberDay

				.setAdviceSpent (
					0l)

				.getAdviceMessages ().add (
					adviceMessage);

		}

	}

	// private implementation

	private
	SmsSpendLimiterNumberDayRec numberToday (
			@NonNull SmsSpendLimiterNumberRec spendLimiterNumber) {

		Transaction transaction =
			database.currentTransaction ();

		SmsSpendLimiterRec spendLimiter =
			spendLimiterNumber.getSmsSpendLimiter ();

		DateTimeZone timezone =
			DateTimeZone.forID (
				spendLimiter.getTimezone ());

		LocalDate today =
			transaction.now ().toDateTime (
				timezone
			).toLocalDate ();

		if (

			isNotNull (
				spendLimiterNumber.getLastSpendDate ())
				
			&& notEarlierThan (
				spendLimiterNumber.getLastSpendDate (),
				today)
		) {

			return smsSpendLimiterNumberDayHelper.find (
				spendLimiterNumber,
				spendLimiterNumber.getLastSpendDate ());

		} else {

			spendLimiterNumber

				.setLastDailySpend (
					0l)

				.setLastSpendDate (
					today);

			return smsSpendLimiterNumberDayHelper.insert (
				smsSpendLimiterNumberDayHelper.createInstance ()

				.setSmsSpendLimiterNumber (
					spendLimiterNumber)

				.setDate (
					today)

				.setTotalSpent (
					0l)

				.setAdviceSpent (
					0l)

				.setLimitSent (
					false)

			);

		}

	}

}
