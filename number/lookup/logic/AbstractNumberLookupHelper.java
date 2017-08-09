package wbs.sms.number.lookup.logic;

import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;

import org.apache.commons.lang3.tuple.Pair;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.sms.number.core.model.NumberRec;
import wbs.sms.number.lookup.model.NumberLookupRec;

public abstract
class AbstractNumberLookupHelper
	implements NumberLookupHelper {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	Pair <List <NumberRec>, List <NumberRec>> splitNumbersPresent (
			@NonNull Transaction parentTransaction,
			@NonNull NumberLookupRec numberLookup,
			@NonNull List <NumberRec> numbers) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"splitNumbersPresent");

		) {

			List <NumberRec> numbersPresent =
				new ArrayList<> ();

			List <NumberRec> numbersNotPresent =
				new ArrayList<> ();

			for (
				NumberRec number
					: numbers
			) {

				if (
					lookupNumber (
						transaction,
						numberLookup,
						number)
				) {

					numbersPresent.add (
						number);

				} else {

					numbersNotPresent.add (
						number);

				}

			}

			return Pair.of (
				numbersPresent,
				numbersNotPresent);

		}

	}

}
