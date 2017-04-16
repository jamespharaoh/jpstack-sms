package wbs.sms.number.blacklist.console;

import static wbs.utils.etc.OptionalUtils.optionalIsPresent;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserObjectHelper;

import wbs.sms.number.blacklist.model.BlacklistObjectHelper;
import wbs.sms.number.blacklist.model.BlacklistRec;
import wbs.sms.number.format.logic.NumberFormatLogic;
import wbs.sms.number.format.logic.WbsNumberFormatException;
import wbs.sms.number.format.model.NumberFormatObjectHelper;
import wbs.sms.number.format.model.NumberFormatRec;

import wbs.web.responder.Responder;

@PrototypeComponent ("blacklistAddAction")
public
class BlacklistAddAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	BlacklistObjectHelper blacklistHelper;

	@SingletonDependency
	Database database;

	@SingletonDependency
	EventLogic eventLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	NumberFormatObjectHelper numberFormatHelper;

	@SingletonDependency
	NumberFormatLogic numberFormatLogic;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserObjectHelper userHelper;

	// details

	@Override
	public
	Responder backupResponder (
			@NonNull TaskLogger parentTaskLogger) {

		return responder (
			"blacklistAddResponder");

	}

	// implementation

	@Override
	public
	Responder goReal (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"goReal");

		try (

			Transaction transaction =
				database.beginReadWrite (
					taskLogger,
					"BlacklistAddAction.goReal ()",
					this);

		) {

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
				optionalIsPresent (
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
				taskLogger,
				blacklistHelper.createInstance ()

				.setNumber (
					number)

				.setReason (
					reason)

			);

			// create an event

			eventLogic.createEvent (
				taskLogger,
				"number_blacklisted",
				userConsoleLogic.userRequired (),
				blacklistOptional.get ());

			transaction.commit ();

			requestContext.addNotice (
				"Added to blacklist");

			return null;

		}

	}

}
