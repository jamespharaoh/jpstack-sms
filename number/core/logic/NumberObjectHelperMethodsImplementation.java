package wbs.sms.number.core.logic;

import static wbs.utils.etc.OptionalUtils.optionalIsPresent;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.entity.record.GlobalId;

import wbs.sms.network.model.NetworkObjectHelper;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.number.core.model.NumberObjectHelper;
import wbs.sms.number.core.model.NumberObjectHelperMethods;
import wbs.sms.number.core.model.NumberRec;

public
class NumberObjectHelperMethodsImplementation
	implements NumberObjectHelperMethods {

	// singleton dependencies

	@WeakSingletonDependency
	NetworkObjectHelper networkHelper;

	@WeakSingletonDependency
	NumberObjectHelper numberHelper;

	// implementation

	@Override
	public
	NumberRec findOrCreate (
			@NonNull String numberString) {

		// find existing

		Optional <NumberRec> numberRecordOptional =
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