package wbs.sms.number.core.logic;

import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.OptionalUtils.optionalOrElseRequired;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.network.model.NetworkObjectHelper;
import wbs.sms.number.core.model.ChatUserNumberReportObjectHelper;
import wbs.sms.number.core.model.ChatUserNumberReportRec;
import wbs.sms.number.core.model.NumberObjectHelper;
import wbs.sms.number.core.model.NumberRec;

@SingletonComponent ("numberLogic")
public
class NumberLogicImplementation
	implements NumberLogic {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	ChatUserNumberReportObjectHelper chatUserNumberReportHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	NetworkObjectHelper networkHelper;

	@SingletonDependency
	NumberObjectHelper numberHelper;

	// implementation

	@Override
	public
	void updateDeliveryStatusForNumber (
			@NonNull Transaction parentTransaction,
			@NonNull String numTo,
			@NonNull MessageStatus status) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"updateDeliveryStatusForNumber");

		) {

			NumberRec number =
				numberHelper.findOrCreate (
					transaction,
					numTo);

			// TODO should not be here

			ChatUserNumberReportRec numberReport =
				optionalOrElseRequired (

				chatUserNumberReportHelper.find (
					transaction,
					number.getId ()),

				() -> chatUserNumberReportHelper.insert (
					transaction,
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

	}

	@Override
	public
	NumberRec archiveNumberFromMessage (
			@NonNull Transaction parentTransaction,
			@NonNull MessageRec message) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"archiveNumberFromMessage");

		) {

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

			transaction.flush ();

			// create new number and save

			NumberRec newNumber =
				numberHelper.insert (
					transaction,
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

			transaction.flush ();

			transaction.warningFormat (
				"Archived number %s as %s",
				currentNumber,
				oldNumber.getNumber ());

			return newNumber;

		}

	}

}
