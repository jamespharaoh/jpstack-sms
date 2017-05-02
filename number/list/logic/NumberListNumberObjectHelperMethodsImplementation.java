package wbs.sms.number.list.logic;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.sms.number.core.model.NumberRec;
import wbs.sms.number.list.model.NumberListNumberObjectHelper;
import wbs.sms.number.list.model.NumberListNumberObjectHelperMethods;
import wbs.sms.number.list.model.NumberListNumberRec;
import wbs.sms.number.list.model.NumberListRec;

public
class NumberListNumberObjectHelperMethodsImplementation
	implements NumberListNumberObjectHelperMethods {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@WeakSingletonDependency
	NumberListNumberObjectHelper numberListNumberHelper;

	// implementation

	@Override
	public
	NumberListNumberRec findOrCreate (
			@NonNull Transaction parentTransaction,
			@NonNull NumberListRec numberList,
			@NonNull NumberRec number) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findOrCreate");

		) {

			// find existing

			NumberListNumberRec numberListNumber =
				numberListNumberHelper.find (
					transaction,
					numberList,
					number);

			if (numberListNumber != null) {
				return numberListNumber;
			}

			// create new

			numberListNumber =
				numberListNumberHelper.insert (
					transaction,
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

}