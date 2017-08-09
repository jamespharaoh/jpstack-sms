package wbs.sms.number.blacklist.console;

import static wbs.utils.etc.OptionalUtils.optionalIsPresent;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NamedDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
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

import wbs.web.responder.WebResponder;

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

	// prototype dependencies

	@PrototypeDependency
	@NamedDependency ("blacklistAddResponder")
	ComponentProvider <WebResponder> addResponderProvider;

	// details

	@Override
	public
	WebResponder backupResponder (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"backupResponder");

		) {

			return addResponderProvider.provide (
				taskLogger);

		}

	}

	// implementation

	@Override
	public
	WebResponder goReal (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"goReal");

		) {

			// TODO this is messy

			NumberFormatRec ukNumberFormat =
				numberFormatHelper.findByCodeRequired (
					transaction,
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

			Optional <BlacklistRec> blacklistOptional =
				blacklistHelper.findByCode (
					transaction,
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
				transaction,
				blacklistHelper.createInstance ()

				.setNumber (
					number)

				.setReason (
					reason)

			);

			// create an event

			eventLogic.createEvent (
				transaction,
				"number_blacklisted",
				userConsoleLogic.userRequired (
					transaction),
				blacklistOptional.get ());

			transaction.commit ();

			requestContext.addNotice (
				"Added to blacklist");

			return null;

		}

	}

}
