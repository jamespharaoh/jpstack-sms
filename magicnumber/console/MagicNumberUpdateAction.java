package wbs.sms.magicnumber.console;

import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.isPresent;
import static wbs.framework.utils.etc.Misc.notEqual;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;

import javax.inject.Inject;
import javax.servlet.ServletException;

import lombok.Cleanup;

import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.sms.magicnumber.model.MagicNumberRec;
import wbs.sms.magicnumber.model.MagicNumberSetRec;
import wbs.sms.number.format.logic.NumberFormatLogic;
import wbs.sms.number.format.logic.WbsNumberFormatException;

@PrototypeComponent ("magicNumberUpdateAction")
public
class MagicNumberUpdateAction
	extends ConsoleAction {

	// dependencies

	@Inject
	Database database;

	@Inject
	EventLogic eventLogic;

	@Inject
	MagicNumberConsoleHelper magicNumberHelper;

	@Inject
	MagicNumberSetConsoleHelper magicNumberSetHelper;

	@Inject
	NumberFormatLogic numberFormatLogic;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	UserConsoleLogic userConsoleLogic;

	// details

	@Override
	protected
	Responder backupResponder () {

		return responder (
			"magicNumberUpdateResponder");

	}

	// implementation

	@Override
	protected
	Responder goReal ()
		throws ServletException {

		// start transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"MagicNumberUpdateAction.goReal ()",
				this);

		MagicNumberSetRec magicNumberSet =
			magicNumberSetHelper.findRequired (
				requestContext.stuffInt (
					"magicNumberSetId"));

		// parse numbers

		List<String> numbers;

		try {

			numbers =
				numberFormatLogic.parseLines (
					magicNumberSet.getNumberFormat (),
					requestContext.parameterRequired (
						"numbers"));

		} catch (WbsNumberFormatException exception) {

			requestContext.addNotice (
				"Invalid number format");

			return null;

		}

		if (
			isPresent (
				requestContext.parameter (
					"create"))
		) {

			// add numbers

			int numAdded = 0;

			for (
				String number
					: numbers
			) {

				MagicNumberRec existingMagicNumber =
					magicNumberHelper.findByNumber (
						number);

				if (
					isNotNull (
						existingMagicNumber)
				) {

					if (
						notEqual (
							existingMagicNumber.getMagicNumberSet (),
							magicNumberSet)
					) {

						requestContext.addNotice (
							stringFormat (
								"Number %s is already allocated to another ",
								number,
								"magic number set"));

						return null;

					}

					if (! existingMagicNumber.getDeleted ()) {
						continue;
					}

					existingMagicNumber

						.setDeleted (
							false);

					numAdded ++;

					eventLogic.createEvent (
						"object_field_updated",
						userConsoleLogic.userRequired (),
						"deleted",
						existingMagicNumber,
						false);

				} else {

					MagicNumberRec newMagicNumber =
						magicNumberHelper.insert (
							magicNumberHelper.createInstance ()

						.setMagicNumberSet (
							magicNumberSet)

						.setNumber (
							number)

					);

					numAdded ++;

					eventLogic.createEvent (
						"object_created",
						userConsoleLogic.userRequired (),
						newMagicNumber,
						magicNumberSet);

				}

			}

			// commit transaction

			transaction.commit ();

			// messages

			if (numAdded > 0) {

				requestContext.addNotice (
					stringFormat (
						"%s magic numbers created",
						numAdded));

			}

		} else if (
			isPresent (
				requestContext.parameter (
					"delete"))
		) {

			// delete numbers

			int numDeleted = 0;

			for (
				String number
					: numbers
			) {

				MagicNumberRec magicNumber =
					magicNumberHelper.findByNumber (
						number);

				if (
					notEqual (
						magicNumber.getMagicNumberSet (),
						magicNumberSet)
				) {
					continue;
				}

				if (magicNumber.getDeleted ()) {
					continue;
				}

				magicNumber

					.setDeleted (
						true);

				numDeleted ++;

				eventLogic.createEvent (
					"object_field_updated",
					userConsoleLogic.userRequired (),
					"deleted",
					magicNumber,
					true);

			}

			// commit transaction

			transaction.commit ();

			// messages

			if (numDeleted > 0) {

				requestContext.addNotice (
					stringFormat (
						"%s magic numbers deleted",
						numDeleted));

			}

		}

		return null;

	}

}
