package wbs.sms.number.list.logic;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

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
			@NonNull TaskLogger parentTaskLogger,
			@NonNull NumberListRec numberList,
			@NonNull NumberRec number) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"findOrCreate");

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
				taskLogger,
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