package wbs.sms.spendlimit.logic;

import static wbs.utils.etc.Misc.isNotNull;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.sms.number.core.model.NumberRec;
import wbs.sms.spendlimit.model.SmsSpendLimiterNumberObjectHelper;
import wbs.sms.spendlimit.model.SmsSpendLimiterNumberObjectHelperMethods;
import wbs.sms.spendlimit.model.SmsSpendLimiterNumberRec;
import wbs.sms.spendlimit.model.SmsSpendLimiterRec;

public
class SmsSpendLimiterNumberObjectHelperMethodsImplementation
	implements SmsSpendLimiterNumberObjectHelperMethods {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@WeakSingletonDependency
	SmsSpendLimiterNumberObjectHelper smsSpendLimiterNumberHelper;

	// public implementation

	@Override
	public
	SmsSpendLimiterNumberRec findOrCreate (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull SmsSpendLimiterRec smsSpendLimiter,
			@NonNull NumberRec number) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"findOrCreate");

		SmsSpendLimiterNumberRec existingSpendLimiterNumber =
			smsSpendLimiterNumberHelper.find (
				smsSpendLimiter,
				number);

		if (
			isNotNull (
				existingSpendLimiterNumber)
		) {
			return existingSpendLimiterNumber;
		}

		return smsSpendLimiterNumberHelper.insert (
			taskLogger,
			smsSpendLimiterNumberHelper.createInstance ()

			.setSmsSpendLimiter (
				smsSpendLimiter)

			.setNumber (
				number)

			.setTotalSpent (
				0l)

			.setAdviceSpent (
				0l)

		);

	}

}
