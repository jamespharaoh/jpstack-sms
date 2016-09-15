package wbs.sms.number.list.logic;

import lombok.NonNull;

import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.number.list.model.NumberListNumberObjectHelper;
import wbs.sms.number.list.model.NumberListNumberObjectHelperMethods;
import wbs.sms.number.list.model.NumberListNumberRec;
import wbs.sms.number.list.model.NumberListRec;

public
class NumberListNumberObjectHelperMethodsImplementation
	implements NumberListNumberObjectHelperMethods {

	// singleton dependencies

	@WeakSingletonDependency
	NumberListNumberObjectHelper numberListNumberHelper;

	// implementation

	@Override
	public
	NumberListNumberRec findOrCreate (
			@NonNull NumberListRec numberList,
			@NonNull NumberRec number) {

		// find existing

		NumberListNumberRec numberListNumber =
			numberListNumberHelper.find (
				numberList,
				number);

		if (numberListNumber != null)
			return numberListNumber;

		// create new

		numberListNumber =
			numberListNumberHelper.insert (
				numberListNumberHelper.createInstance ()

			.setNumberList (
				numberList)

			.setNumber (
				number)

			.setPresent (
				false)

		);

		// return

		return numberListNumber;

	}

}