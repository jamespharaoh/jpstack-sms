package wbs.sms.customer.model;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.utils.RandomLogic;
import wbs.sms.number.core.model.NumberRec;

public
class SmsCustomerObjectHelperImplementation
	implements SmsCustomerObjectHelperMethods {

	// dependencies

	@Inject
	Database database;

	@Inject
	RandomLogic randomLogic;

	// indirect dependencies

	@Inject
	Provider<SmsCustomerObjectHelper> smsCustomerHelperProvider;

	// implementation

	@Override
	public
	SmsCustomerRec findOrCreate (
			SmsCustomerManagerRec manager,
			NumberRec number) {

		Transaction transaction =
			database.currentTransaction ();

		SmsCustomerObjectHelper smsCustomerHelper =
			smsCustomerHelperProvider.get ();

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