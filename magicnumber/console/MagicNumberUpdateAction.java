package wbs.sms.magicnumber.console;

import static wbs.utils.etc.LogicUtils.referenceNotEqualWithClass;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;

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
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.console.UserConsoleLogic;

import wbs.sms.magicnumber.model.MagicNumberRec;
import wbs.sms.magicnumber.model.MagicNumberSetRec;
import wbs.sms.number.format.logic.NumberFormatLogic;
import wbs.sms.number.format.logic.WbsNumberFormatException;

import wbs.web.responder.WebResponder;

@PrototypeComponent ("magicNumberUpdateAction")
public
class MagicNumberUpdateAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	EventLogic eventLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MagicNumberConsoleHelper magicNumberHelper;

	@SingletonDependency
	MagicNumberSetConsoleHelper magicNumberSetHelper;

	@SingletonDependency
	NumberFormatLogic numberFormatLogic;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// prototype dependencies

	@PrototypeDependency
	@NamedDependency ("magicNumberUpdateResponder")
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

		// start transaction

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"goReal");

		) {

			MagicNumberSetRec magicNumberSet =
				magicNumberSetHelper.findFromContextRequired (
					transaction);

			// parse numbers

			List <String> numbers;

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
				optionalIsPresent (
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
							transaction,
							number);

					if (
						isNotNull (
							existingMagicNumber)
					) {

						if (
							referenceNotEqualWithClass (
								MagicNumberSetRec.class,
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
							transaction,
							"object_field_updated",
							userConsoleLogic.userRequired (
								transaction),
							"deleted",
							existingMagicNumber,
							false);

					} else {

						MagicNumberRec newMagicNumber =
							magicNumberHelper.insert (
								transaction,
								magicNumberHelper.createInstance ()

							.setMagicNumberSet (
								magicNumberSet)

							.setNumber (
								number)

						);

						numAdded ++;

						eventLogic.createEvent (
							transaction,
							"object_created",
							userConsoleLogic.userRequired (
								transaction),
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
							integerToDecimalString (
								numAdded)));

				}

			} else if (
				optionalIsPresent (
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
							transaction,
							number);

					if (
						referenceNotEqualWithClass (
							MagicNumberSetRec.class,
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
						transaction,
						"object_field_updated",
						userConsoleLogic.userRequired (
							transaction),
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
							integerToDecimalString (
								numDeleted)));

				}

			}

			return null;

		}

	}

}
