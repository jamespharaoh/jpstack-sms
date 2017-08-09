package wbs.sms.number.list.logic;

import static wbs.utils.etc.TypeUtils.genericCastUnchecked;

import java.util.List;

import lombok.NonNull;

import org.apache.commons.lang3.tuple.Pair;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.object.ObjectManager;

import wbs.sms.number.core.model.NumberRec;
import wbs.sms.number.list.model.NumberListRec;
import wbs.sms.number.lookup.logic.NumberLookupHelper;
import wbs.sms.number.lookup.model.NumberLookupRec;

@SingletonComponent ("numberListNumberLookupHelper")
public
class NumberListNumberLookupHelper
	implements NumberLookupHelper {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	NumberListLogic numberListLogic;

	@SingletonDependency
	ObjectManager objectManager;

	// details

	@Override
	public
	String parentObjectTypeCode () {
		return "number_list";
	}

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

			NumberListRec numberList =
				genericCastUnchecked (
					objectManager.getParentRequired (
						transaction,
						numberLookup));

			return numberListLogic.includesNumber (
				transaction,
				numberList,
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

			NumberListRec numberList =
				genericCastUnchecked (
					objectManager.getParentRequired (
						transaction,
						numberLookup));

			return numberListLogic.splitNumbersPresent (
				transaction,
				numberList,
				numbers);

		}

	}

}
