package wbs.sms.number.core.model;

import static wbs.framework.utils.etc.Misc.isPresent;

import javax.inject.Inject;
import javax.inject.Provider;

import com.google.common.base.Optional;

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

		Optional<NumberRec> numberRecordOptional =
			numberHelper.findByCode (
				GlobalId.root,
				numberString);

		if (
			isPresent (
				numberRecordOptional)
		) {
			return numberRecordOptional.get ();
		}

		// create it

		NetworkRec defaultNetwork =
			networkHelper.findRequired (0);

		return numberHelper.insert (
			numberHelper.createInstance ()

			.setNumber (
				numberString)

			.setNetwork (
				defaultNetwork)

		);

	}

}