package wbs.sms.number.lookup.logic;

import java.util.List;

import lombok.NonNull;

import org.apache.commons.lang3.tuple.Pair;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.sms.number.core.model.NumberRec;
import wbs.sms.number.lookup.model.NumberLookupRec;

@SingletonComponent ("numberLookupManager")
public
class NumberLookupManagerImplementation
	implements NumberLookupManager {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	NumberLookupHelperManager numberLookupHelperManager;

	// implementation

	@Override
	public
	boolean lookupNumber (
			@NonNull Transaction parentTransaction,
			@NonNull NumberLookupRec numberLookup,
			@NonNull NumberRec number) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"lookupNumber");

		) {

			NumberLookupHelper helper =
				numberLookupHelperManager.forParentObjectTypeCode (
					numberLookup.getParentType ().getCode (),
					true);

			return helper.lookupNumber (
				transaction,
				numberLookup,
				number);

		}

	}

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

			NumberLookupHelper helper =
				numberLookupHelperManager.forParentObjectTypeCode (
					numberLookup.getParentType ().getCode (),
					true);

			return helper.splitNumbersPresent (
				transaction,
				numberLookup,
				numbers);

		}

	}

}
