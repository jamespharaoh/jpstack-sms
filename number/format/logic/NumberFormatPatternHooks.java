package wbs.sms.number.format.logic;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.object.ObjectHooks;

import wbs.sms.number.format.model.NumberFormatPatternRec;
import wbs.sms.number.format.model.NumberFormatRec;

public
class NumberFormatPatternHooks
	implements ObjectHooks<NumberFormatPatternRec> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	void beforeInsert (
			@NonNull Transaction parentTransaction,
			@NonNull NumberFormatPatternRec numberFormatPattern) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"beforeInesrt");

		) {

			NumberFormatRec numberFormat =
				numberFormatPattern.getNumberFormat ();

			numberFormatPattern.setIndex (
				numberFormat.getNumPatterns ());

			numberFormat.setNumPatterns (
				numberFormat.getNumPatterns () + 1);

		}

	}

}