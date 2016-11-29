package wbs.sms.spendlimit.logic;

import static wbs.utils.etc.Misc.isNotNull;

import lombok.NonNull;

import wbs.framework.component.annotations.WeakSingletonDependency;

import wbs.sms.number.core.model.NumberRec;
import wbs.sms.spendlimit.model.SmsSpendLimiterNumberObjectHelper;
import wbs.sms.spendlimit.model.SmsSpendLimiterNumberObjectHelperMethods;
import wbs.sms.spendlimit.model.SmsSpendLimiterNumberRec;
import wbs.sms.spendlimit.model.SmsSpendLimiterRec;

public
class SmsSpendLimiterNumberObjectHelperMethodsImplementation
	implements SmsSpendLimiterNumberObjectHelperMethods {

	// singleton dependencies

	@WeakSingletonDependency
	SmsSpendLimiterNumberObjectHelper smsSpendLimiterNumberHelper;

	// public implementation

	@Override
	public
	SmsSpendLimiterNumberRec findOrCreate (
			@NonNull SmsSpendLimiterRec smsSpendLimiter,
			@NonNull NumberRec number) {

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
