package wbs.sms.number.list.console;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;

import java.util.List;

import lombok.NonNull;
import lombok.experimental.Accessors;

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
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.console.UserConsoleLogic;

import wbs.sms.number.core.model.NumberObjectHelper;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.number.format.logic.NumberFormatLogic;
import wbs.sms.number.format.logic.WbsNumberFormatException;
import wbs.sms.number.list.model.NumberListNumberRec;
import wbs.sms.number.list.model.NumberListRec;
import wbs.sms.number.list.model.NumberListUpdateRec;

import wbs.web.responder.WebResponder;

@Accessors (fluent = true)
@PrototypeComponent ("numberListNumberUpdateAction")
public
class NumberListNumberUpdateAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	EventLogic eventLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	NumberFormatLogic numberFormatLogic;

	@SingletonDependency
	NumberObjectHelper numberHelper;

	@SingletonDependency
	NumberListConsoleHelper numberListHelper;

	@SingletonDependency
	NumberListNumberConsoleHelper numberListNumberHelper;

	@SingletonDependency
	NumberListUpdateConsoleHelper numberListUpdateHelper;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// prototype dependencies

	@PrototypeDependency
	@NamedDependency ("numberListNumberUpdateResponder")
	ComponentProvider <WebResponder> updateResponderProvider;

	// details

	@Override
	protected
	WebResponder backupResponder (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"backupResponder");

		) {

			return updateResponderProvider.provide (
				taskLogger);

		}

	}

	// implementation

	@Override
	protected
	WebResponder goReal (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"goReal");

		) {

			int loop = 0;

			NumberListRec numberList =
				numberListHelper.findFromContextRequired (
					transaction);

			NumberListUpdateRec numberListUpdate =
				numberListUpdateHelper.createInstance ()

				.setNumberList (
					numberList)

				.setTimestamp (
					transaction.now ())

				.setUser (
					userConsoleLogic.userRequired (
						transaction))

				.setNumberCount (
					0l);

			// parse numbers

			List<String> numbers;

			try {

				numbers =
					numberFormatLogic.parseLines (
						numberList.getNumberFormat (),
						requestContext.parameterRequired (
							"numbers"));

			} catch (WbsNumberFormatException exception) {

				requestContext.addNotice (
					"Invalid number format");

				return null;

			}

			// add numbers

			int numAdded = 0;
			int numAlreadyAdded = 0;

			if (
				optionalIsPresent (
					requestContext.parameter (
						"add"))
			) {

				numberListUpdate

					.setPresent (
						true);

				for (
					String numberString
						: numbers
				) {

					if (++ loop % 1000 == 0)
						transaction.flush ();

					NumberRec number =
						numberHelper.findOrCreate (
							transaction,
							numberString);

					NumberListNumberRec numberListNumber =
						numberListNumberHelper.findOrCreate (
							transaction,
							numberList,
							number);

					if (numberListNumber.getPresent ()) {

						numAlreadyAdded ++;

						continue;

					}

					numberListNumber

						.setPresent (
							true);

					numberListUpdate

						.setNumberCount (
							numberListUpdate.getNumberCount () + 1);

					numberListUpdate.getNumbers ().add (
						number);

					numberList

						.setNumberCount (
							numberList.getNumberCount () + 1);

					numAdded ++;

				}

			}

			// remove numbers

			int numRemoved = 0;
			int numAlreadyRemoved = 0;

			if (
				optionalIsPresent (
					requestContext.parameter (
						"remove"))
			) {

				numberListUpdate.setPresent (false);

				for (
					String numberString
						: numbers
				) {

					if (++ loop % 1000 == 0)
						transaction.flush ();

					NumberRec number =
						numberHelper.findOrCreate (
							transaction,
							numberString);

					NumberListNumberRec numberListNumber =
						numberListNumberHelper.find (
							transaction,
							numberList,
							number);

					if (numberListNumber == null
							|| ! numberListNumber.getPresent ()) {

						numAlreadyRemoved ++;

						continue;

					}

					numberListNumber.setPresent (
						false);

					numberListUpdate.setNumberCount (
						numberListUpdate.getNumberCount () + 1);

					numberListUpdate.getNumbers ().add (
						number);

					numberList.setNumberCount (
						numberList.getNumberCount () - 1);

					numRemoved ++;

				}

			}

			// insert update

			if (numberListUpdate.getNumberCount () > 0) {

				numberListUpdateHelper.insert (
					transaction,
					numberListUpdate);

			}

			// events

			if (numAdded > 0) {

				eventLogic.createEvent (
					transaction,
					"number_list_numbers_added",
					userConsoleLogic.userRequired (
						transaction),
					numAdded,
					numberList);

			}

			if (numRemoved > 0) {

				eventLogic.createEvent (
					transaction,
					"number_list_numbers_removed",
					userConsoleLogic.userRequired (
						transaction),
					numRemoved,
					numberList);

			}

			// commit transaction

			if (numberListUpdate.getNumberCount () > 0) {

				transaction.commit ();

			}

			// messages

			if (numAdded > 0) {

				requestContext.addNoticeFormat (
					"%s numbers added",
					integerToDecimalString (
						numAdded));

			}

			if (numAlreadyAdded > 0) {

				requestContext.addWarningFormat (
					"%s numbers already added",
					integerToDecimalString (
						numAlreadyAdded));

			}

			if (numRemoved > 0) {

				requestContext.addNoticeFormat (
					"%s numbers removed",
					integerToDecimalString (
						numRemoved));

			}

			if (numAlreadyRemoved > 0) {

				requestContext.addWarningFormat (
					"%s numbers already removed",
					integerToDecimalString (
						numAlreadyRemoved));

			}

			return null;

		}

	}

}
