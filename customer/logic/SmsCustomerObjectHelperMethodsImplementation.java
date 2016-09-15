package wbs.sms.customer.logic;

import lombok.NonNull;

import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
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

	@SingletonDependency
	RandomLogic randomLogic;

	@WeakSingletonDependency
	SmsCustomerObjectHelper smsCustomerHelper;

	// implementation

	@Override
	public
	SmsCustomerRec findOrCreate (
			@NonNull SmsCustomerManagerRec manager,
			@NonNull NumberRec number) {

		Transaction transaction =
			database.currentTransaction ();

		SmsCustomerRec customer =
			smsCustomerHelper.find (
				manager,
				number);

		if (customer != null)
			return customer;

		customer =
			smsCustomerHelper.insert (
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