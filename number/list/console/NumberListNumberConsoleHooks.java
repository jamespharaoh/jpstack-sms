package wbs.sms.number.list.console;

import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.helper.core.ConsoleHooks;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.WeakSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.sms.number.list.model.NumberListNumberRec;
import wbs.sms.number.list.model.NumberListNumberSearch;
import wbs.sms.number.list.model.NumberListRec;

@SingletonComponent ("numberListNumberConsoleHooks")
public
class NumberListNumberConsoleHooks
	implements ConsoleHooks <NumberListNumberRec> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@WeakSingletonDependency
	NumberListConsoleHelper numberListHelper;

	// implementation

	@Override
	public
	void applySearchFilter (
			@NonNull Transaction parentTransaction,
			@NonNull Object searchObject) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"applySearchFilter");

		) {

			NumberListNumberSearch search =
				genericCastUnchecked (
					searchObject);

			Optional <NumberListRec> numberListOptional =
				numberListHelper.findFromContext (
					transaction);

			if (
				optionalIsPresent (
					numberListOptional)
			) {

				NumberListRec numberList =
					optionalGetRequired (
						numberListOptional);

				search

					.numberListId (
						numberList.getId ())

				;

			}

			search

				.present (
					true)

			;

		}

	}

}
