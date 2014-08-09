package wbs.sms.magicnumber.console;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;

import javax.inject.Inject;
import javax.servlet.ServletException;

import lombok.Cleanup;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.console.action.ConsoleAction;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.console.UserConsoleHelper;
import wbs.platform.user.model.UserRec;
import wbs.sms.magicnumber.model.MagicNumberRec;
import wbs.sms.magicnumber.model.MagicNumberSetRec;
import wbs.sms.number.format.logic.NumberFormatLogic;
import wbs.sms.number.format.logic.WbsNumberFormatException;

@PrototypeComponent ("magicNumberCreateAction")
public
class MagicNumberCreateAction
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
	UserConsoleHelper userHelper;

	// details

	@Override
	protected
	Responder backupResponder () {
		return responder ("magicNumberCreateResponder");
	}

	// implementation

	@Override
	protected
	Responder goReal ()
		throws ServletException {

		// start transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		UserRec myUser =
			userHelper.find (
				requestContext.userId ());

		MagicNumberSetRec magicNumberSet =
			magicNumberSetHelper.find (
				requestContext.stuffInt ("magicNumberSetId"));

		// parse numbers

		List<String> numbers;

		try {

			numbers =
				numberFormatLogic.parseLines (
					magicNumberSet.getNumberFormat (),
					requestContext.parameter ("numbers"));

		} catch (WbsNumberFormatException exception) {

			requestContext.addNotice (
				"Invalid number format");

			return null;

		}

		// add numbers

		int numAdded = 0;

		for (String number : numbers) {

			MagicNumberRec magicNumber =
				magicNumberHelper.insert (
					new MagicNumberRec ()
						.setMagicNumberSet (magicNumberSet)
						.setNumber (number));

			numAdded ++;

			eventLogic.createEvent (
				"object_created",
				myUser,
				magicNumber,
				magicNumberSet);

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

		return null;

	}

}
