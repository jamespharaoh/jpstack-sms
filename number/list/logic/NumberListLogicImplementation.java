package wbs.sms.number.list.logic;

import static wbs.utils.collection.IterableUtils.iterableMapToSet;
import static wbs.utils.etc.Misc.contains;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import lombok.NonNull;

import org.apache.commons.lang3.tuple.Pair;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.platform.service.model.ServiceRec;

import wbs.sms.message.core.model.MessageRec;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.number.list.model.NumberListNumberObjectHelper;
import wbs.sms.number.list.model.NumberListNumberRec;
import wbs.sms.number.list.model.NumberListObjectHelper;
import wbs.sms.number.list.model.NumberListRec;
import wbs.sms.number.list.model.NumberListUpdateObjectHelper;

@SingletonComponent ("numberListLogic")
public
class NumberListLogicImplementation
	implements NumberListLogic {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	NumberListObjectHelper numberListHelper;

	@SingletonDependency
	NumberListNumberObjectHelper numberListNumberHelper;

	@SingletonDependency
	NumberListUpdateObjectHelper numberListUpdateHelper;

	// implementation

	@Override
	public
	void addDueToMessage (
			@NonNull Transaction parentTransaction,
			@NonNull NumberListRec numberList,
			@NonNull NumberRec number,
			@NonNull MessageRec message,
			@NonNull ServiceRec service) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"addDueToMessage");

		) {

			// lookup number list number

			NumberListNumberRec numberListNumber =
				numberListNumberHelper.findOrCreate (
					transaction,
					numberList,
					number);

			// do nothing if already present

			if (
				numberListNumber.getPresent ()
			) {
				return;
			}

			// update number list number

			numberListNumber

				.setPresent (
					true);

			// create number list update

			numberListUpdateHelper.insert (
				transaction,
				numberListUpdateHelper.createInstance ()

				.setNumberList (
					numberList)

				.setTimestamp (
					transaction.now ())

				.setService (
					service)

				.setMessage (
					message)

				.setPresent (
					true)

				.setNumberCount (
					1l)

				.setNumbers (
					Collections.singleton (
						number))

			);

			// update number list

			numberList

				.setNumberCount (
					numberList.getNumberCount () + 1);

		}

	}

	@Override
	public
	boolean includesNumber (
			@NonNull Transaction parentTransaction,
			@NonNull NumberListRec numberList,
			@NonNull NumberRec number) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"includesNumber");

		) {

			NumberListNumberRec numberListNumber =
				numberListNumberHelper.find (
					transaction,
					numberList,
					number);

			if (numberListNumber == null)
				return false;

			return numberListNumber.getPresent ();

		}

	}

	@Override
	public
	Pair <List <NumberRec>, List <NumberRec>> splitNumbersPresent (
			@NonNull Transaction parentTransaction,
			@NonNull NumberListRec numberList,
			@NonNull List <NumberRec> numbers) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"splitNumbersPresent");

		) {

			List <NumberListNumberRec> numberListNumbers =
				numberListNumberHelper.findManyPresent (
					transaction,
					numberList,
					numbers);

			Set <Long> numberIdsPresent =
				iterableMapToSet (
					numberListNumber ->
						numberListNumber.getNumber ().getId (),
					numberListNumbers);

			List <NumberRec> numbersPresent =
				new ArrayList<> ();

			List <NumberRec> numbersNotPresent =
				new ArrayList<> ();

			for (
				NumberRec number
					: numbers
			) {

				if (
					contains (
						numberIdsPresent,
						number.getId ())
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

	@Override
	public
	void removeDueToMessage (
			@NonNull Transaction parentTransaction,
			@NonNull NumberListRec numberList,
			@NonNull NumberRec number,
			@NonNull MessageRec message,
			@NonNull ServiceRec service) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"removeDueToMessage");

		) {

			// lookup number list number

			NumberListNumberRec numberListNumber =
				numberListNumberHelper.findOrCreate (
					transaction,
					numberList,
					number);

			// do nothing if not present

			if (
				! numberListNumber.getPresent ()
			) {
				return;
			}

			// update number list number

			numberListNumber

				.setPresent (
					false);

			// create number list update

			numberListUpdateHelper.insert (
				transaction,
				numberListUpdateHelper.createInstance ()

				.setNumberList (
					numberList)

				.setTimestamp (
					transaction.now ())

				.setService (
					service)

				.setMessage (
					message)

				.setPresent (
					false)

				.setNumberCount (
					1l)

				.setNumbers (
					Collections.singleton (
						number))

			);

			// update number list

			numberList

				.setNumberCount (
					numberList.getNumberCount () - 1);

		}

	}

}
