package wbs.sms.number.list.model;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.NonNull;

import wbs.sms.number.core.model.NumberRec;

public
class NumberListNumberObjectHelperImplementation
	implements NumberListNumberObjectHelperMethods {

	@Inject
	Provider<NumberListNumberObjectHelper> numberListNumberHelper;

	@Override
	public
	NumberListNumberRec find (
			@NonNull NumberListRec numberList,
			@NonNull NumberRec number) {

		return numberListNumberHelper.get ().findByNumberListAndNumber (
			numberList.getId (),
			number.getId ());

	}

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