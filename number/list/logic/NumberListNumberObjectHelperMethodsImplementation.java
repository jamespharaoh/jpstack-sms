package wbs.sms.number.list.logic;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.NonNull;

import wbs.sms.number.core.model.NumberRec;
import wbs.sms.number.list.model.NumberListNumberObjectHelper;
import wbs.sms.number.list.model.NumberListNumberObjectHelperMethods;
import wbs.sms.number.list.model.NumberListNumberRec;
import wbs.sms.number.list.model.NumberListRec;

public
class NumberListNumberObjectHelperMethodsImplementation
	implements NumberListNumberObjectHelperMethods {

	@Inject
	Provider<NumberListNumberObjectHelper> numberListNumberHelper;

	@Override
	public
	NumberListNumberRec findOrCreate (
			@NonNull NumberListRec numberList,
			@NonNull NumberRec number) {

		// find existing

		NumberListNumberRec numberListNumber =
			numberListNumberHelper.get ().find (
				numberList,
				number);

		if (numberListNumber != null)
			return numberListNumber;

		// create new

		numberListNumber =
			numberListNumberHelper.get ().insert (
				numberListNumberHelper.get ().createInstance ()

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