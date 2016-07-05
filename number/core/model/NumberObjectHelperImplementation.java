package wbs.sms.number.core.model;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.framework.record.GlobalId;
import wbs.sms.network.model.NetworkObjectHelper;
import wbs.sms.network.model.NetworkRec;

public
class NumberObjectHelperImplementation
	implements NumberObjectHelperMethods {

	// indirect dependencies

	@Inject
	Provider<NetworkObjectHelper> networkHelperProvider;

	@Inject
	Provider<NumberObjectHelper> numberHelperProvider;

	// implementation

	@Override
	public
	NumberRec findOrCreate (
			String numberString) {

		NetworkObjectHelper networkHelper =
			networkHelperProvider.get ();

		NumberObjectHelper numberHelper =
			numberHelperProvider.get ();

		// find existing

		NumberRec numberRecord =
			numberHelper.findByCodeOrNull (
				GlobalId.root,
				numberString);

		if (numberRecord != null)
			return numberRecord;

		// create it

		NetworkRec defaultNetwork =
			networkHelper.findOrNull (0);

		return numberHelper.insert (
			numberHelper.createInstance ()

			.setNumber (
				numberString)

			.setNetwork (
				defaultNetwork)

		);

	}

}