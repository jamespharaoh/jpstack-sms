package wbs.sms.number.list.logic;

import java.util.Collections;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
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
			NumberListRec numberList,
			NumberRec number,
			MessageRec message,
			ServiceRec service) {

		Transaction transaction =
			database.currentTransaction ();

		// lookup number list number

		NumberListNumberRec numberListNumber =
			numberListNumberHelper.findOrCreate (
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

	@Override
	public
	boolean includesNumber (
			NumberListRec numberList,
			NumberRec number) {

		NumberListNumberRec numberListNumber =
			numberListNumberHelper.find (
				numberList,
				number);

		if (numberListNumber == null)
			return false;

		return numberListNumber.getPresent ();

	}

	@Override
	public
	void removeDueToMessage (
			NumberListRec numberList,
			NumberRec number,
			MessageRec message,
			ServiceRec service) {

		Transaction transaction =
			database.currentTransaction ();

		// lookup number list number

		NumberListNumberRec numberListNumber =
			numberListNumberHelper.findOrCreate (
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
