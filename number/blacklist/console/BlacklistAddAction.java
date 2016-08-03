package wbs.sms.number.blacklist.console;

import javax.inject.Inject;

import lombok.Cleanup;

import com.google.common.base.Optional;

import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.record.GlobalId;
import static wbs.framework.utils.etc.OptionalUtils.isPresent;
import wbs.framework.web.Responder;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserObjectHelper;
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
	UserConsoleLogic userConsoleLogic;

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
				"BlacklistAddAction.goReal ()",
				this);

		// TODO this is messy

		NumberFormatRec ukNumberFormat =
			numberFormatHelper.findByCodeRequired (
				GlobalId.root,
				"uk");

		String number;

		try {

			number =
				numberFormatLogic.parse (
					ukNumberFormat,
					requestContext.parameterRequired (
						"number"));

		} catch (WbsNumberFormatException exception) {

			requestContext.addError (
				"Invalid mobile number");

			return null;

		}

		Optional<BlacklistRec> blacklistOptional =
			blacklistHelper.findByCode (
				GlobalId.root,
				number);

		if (
			isPresent (
				blacklistOptional)
		) {

			requestContext.addError (
				"Number is already blacklisted");

			return null;

		}

		String reason =
			requestContext.parameterRequired (
				"reason");

		if (reason.length() < 5) {

			requestContext.addError (
				"You must provide a substantial reason");

			return null;

		}

		blacklistHelper.insert (
			blacklistHelper.createInstance ()

			.setNumber (
				number)

			.setReason (
				reason)

		);

		// create an event

		eventLogic.createEvent (
			"number_blacklisted",
			userConsoleLogic.userRequired (),
			blacklistOptional.get ());

		transaction.commit ();

		requestContext.addNotice (
			"Added to blacklist");

		return null;

	}

}
