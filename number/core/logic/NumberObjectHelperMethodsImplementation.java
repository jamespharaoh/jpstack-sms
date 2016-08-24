package wbs.sms.number.core.logic;

import static wbs.framework.utils.etc.OptionalUtils.optionalIsPresent;

import javax.inject.Inject;
import javax.inject.Provider;

import com.google.common.base.Optional;

import wbs.framework.entity.record.GlobalId;
import wbs.sms.network.model.NetworkObjectHelper;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.number.core.model.NumberObjectHelper;
import wbs.sms.number.core.model.NumberObjectHelperMethods;
import wbs.sms.number.core.model.NumberRec;

public
class NumberObjectHelperMethodsImplementation
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
			optionalIsPresent (
				numberRecordOptional)
		) {
			return numberRecordOptional.get ();
		}

		// create it

		NetworkRec defaultNetwork =
			networkHelper.findRequired (
				0l);

		return numberHelper.insert (
			numberHelper.createInstance ()

			.setNumber (
				numberString)

			.setNetwork (
				defaultNetwork)

		);

	}

}