package wbs.sms.customer.logic;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.sms.customer.model.SmsCustomerManagerRec;
import wbs.sms.customer.model.SmsCustomerObjectHelper;
import wbs.sms.customer.model.SmsCustomerObjectHelperMethods;
import wbs.sms.customer.model.SmsCustomerRec;
import wbs.sms.number.core.model.NumberRec;

import wbs.utils.random.RandomLogic;

public
class SmsCustomerObjectHelperMethodsImplementation
	implements SmsCustomerObjectHelperMethods {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RandomLogic randomLogic;

	@WeakSingletonDependency
	SmsCustomerObjectHelper smsCustomerHelper;

	// implementation

	@Override
	public
	SmsCustomerRec findOrCreate (
			@NonNull Transaction parentTransaction,
			@NonNull SmsCustomerManagerRec manager,
			@NonNull NumberRec number) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findOrCreate");

		) {

			SmsCustomerRec customer =
				smsCustomerHelper.find (
					transaction,
					manager,
					number);

			if (customer != null)
				return customer;

			customer =
				smsCustomerHelper.insert (
					transaction,
					smsCustomerHelper.createInstance ()

				.setSmsCustomerManager (
					manager)

				.setNumber (
					number)

				.setCode (
					randomLogic.generateNumericNoZero (6))

				.setCreatedTime (
					transaction.now ())

			);

			return customer;

		}

	}

}