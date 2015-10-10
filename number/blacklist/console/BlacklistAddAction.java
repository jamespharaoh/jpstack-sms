package wbs.sms.number.blacklist.console;

import javax.inject.Inject;

import lombok.Cleanup;
import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.record.GlobalId;
import wbs.framework.web.Responder;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;
import wbs.sms.number.blacklist.model.BlacklistObjectHelper;
import wbs.sms.number.blacklist.model.BlacklistRec;
import wbs.sms.number.format.logic.NumberFormatLogic;
import wbs.sms.number.format.logic.WbsNumberFormatException;
import wbs.sms.number.format.model.NumberFormatObjectHelper;
import wbs.sms.number.format.model.NumberFormatRec;

@PrototypeComponent ("blacklistAddAction")
public
class BlacklistAddAction
	extends ConsoleAction {

	// dependencies

	@Inject
	BlacklistObjectHelper blacklistHelper;

	@Inject
	Database database;

	@Inject
	EventLogic eventLogic;

	@Inject
	NumberFormatObjectHelper numberFormatHelper;

	@Inject
	NumberFormatLogic numberFormatLogic;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	UserObjectHelper userHelper;

	// details

	@Override
	public
	Responder backupResponder () {

		return responder ("blacklistAddResponder");

	}

	// implementation

	@Override
	public
	Responder goReal () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		// TODO this is messy

		NumberFormatRec ukNumberFormat =
			numberFormatHelper.findByCode (
				GlobalId.root,
				"uk");

		String number;

		try {

			number =
				numberFormatLogic.parse (
					ukNumberFormat,
					requestContext.parameter ("number"));

		} catch (WbsNumberFormatException exception) {

			requestContext.addError (
				"Invalid mobile number");

			return null;

		}

		BlacklistRec blacklist =
			blacklistHelper.findByCode (
				GlobalId.root,
				number);

		if (blacklist != null) {

			requestContext.addError (
				"Number is already blacklisted");

			return null;

		}

		String reason =
			requestContext.parameter ("reason");

		if (reason.length() < 5) {
			requestContext.addError("You must provide a substantial reason");
			return null;
		}

		blacklistHelper.insert (
			new BlacklistRec ()
				.setNumber (number)
				.setReason (reason));

		// create an event

		UserRec myUser =
			userHelper.find (
				requestContext.userId ());

		eventLogic.createEvent (
			"number_blacklisted",
			myUser,
			blacklist);

		transaction.commit ();

		requestContext.addNotice (
			"Added to blacklist");

		return null;

	}

}
