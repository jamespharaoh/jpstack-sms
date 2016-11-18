package wbs.sms.number.core.logic;

import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.OptionalUtils.optionalOrElse;
import static wbs.utils.string.StringUtils.stringFormat;

import lombok.extern.log4j.Log4j;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;

import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.network.model.NetworkObjectHelper;
import wbs.sms.number.core.model.ChatUserNumberReportObjectHelper;
import wbs.sms.number.core.model.ChatUserNumberReportRec;
import wbs.sms.number.core.model.NumberObjectHelper;
import wbs.sms.number.core.model.NumberRec;

@Log4j
@SingletonComponent ("numberLogic")
public
class NumberLogicImplementation
	implements NumberLogic {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	NetworkObjectHelper networkHelper;

	@SingletonDependency
	ChatUserNumberReportObjectHelper chatUserNumberReportHelper;

	@SingletonDependency
	NumberObjectHelper numberHelper;

	// implementation

	@Override
	public
	NumberRec objectToNumber (
			Object object) {

		if (object instanceof NumberRec) {

			return
				(NumberRec)
				object;

		}

		if (object instanceof String) {

			return numberHelper.findOrCreate (
				(String) object);

		}

		throw new IllegalArgumentException ();

	}

	@Override
	public
	void updateDeliveryStatusForNumber (
			String numTo,
			MessageStatus status) {

		Transaction transaction =
			database.currentTransaction ();

		NumberRec number =
			numberHelper.findOrCreate (
				numTo);

		// TODO should not be here

		ChatUserNumberReportRec numberReport =
			optionalOrElse (

			chatUserNumberReportHelper.find (
				number.getId ()),

			() -> chatUserNumberReportHelper.insert (
				chatUserNumberReportHelper.createInstance ()

				.setNumber (
					number)

			)

		);

		if (status.isGoodType ()) {

			numberReport

				.setLastSuccess (
					transaction.now ());

		} else if (
			status.isBadType ()
			|| status.isPending ()
		) {

			if (
				isNull (
					numberReport.getFirstFailure ())
			) {

				numberReport

					.setFirstFailure (
						transaction.now ());

			}

		}

	}

	@Override
	public
	NumberRec archiveNumberFromMessage (
			MessageRec message) {

		Transaction transaction =
			database.currentTransaction ();

		// TODO i don't like this at all

		NumberRec oldNumber =
			message.getNumber ();

		String currentNumber =
			oldNumber.getNumber ();

		// re-name old number

		oldNumber

			.setArchiveDate (
				transaction.now ())

			.setNumber (
				currentNumber + "." + oldNumber.getId ());

		database.flush ();

		// create new number and save

		NumberRec newNumber =
			numberHelper.insert (
				numberHelper.createInstance ()

			.setNumber (
				currentNumber)

			.setNetwork (
				oldNumber.getNetwork ())

		);

		// assign message to new number

		message

			.setNumber (
				newNumber);

		database.flush ();

		log.warn (
			stringFormat (
				"Archived number %s as %s",
				currentNumber,
				oldNumber.getNumber ()));

		return newNumber;

	}

}
