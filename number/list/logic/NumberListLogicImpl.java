package wbs.sms.number.list.logic;

import java.util.Collections;

import javax.inject.Inject;

import wbs.framework.application.annotations.SingletonComponent;
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
import wbs.sms.number.list.model.NumberListUpdateRec;

@SingletonComponent ("numberListLogic")
public
class NumberListLogicImpl
	implements NumberListLogic {

	// dependencies

	@Inject
	Database database;

	@Inject
	NumberListObjectHelper numberListHelper;

	@Inject
	NumberListNumberObjectHelper numberListNumberHelper;

	@Inject
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
			new NumberListUpdateRec ()

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
				1)

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
			new NumberListUpdateRec ()

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
				1)

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
