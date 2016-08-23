package wbs.sms.messageset.console;

import static wbs.framework.utils.etc.LogicUtils.referenceNotEqualSafe;
import static wbs.framework.utils.etc.Misc.isNull;
import static wbs.framework.utils.etc.NumberUtils.toJavaIntegerRequired;
import static wbs.framework.utils.etc.OptionalUtils.isNotPresent;
import static wbs.framework.utils.etc.OptionalUtils.isPresent;
import static wbs.framework.utils.etc.StringUtils.stringFormat;
import static wbs.framework.utils.etc.StringUtils.stringIsEmpty;
import static wbs.framework.utils.etc.StringUtils.stringNotEqualSafe;

import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j;
import wbs.console.action.ConsoleAction;
import wbs.console.lookup.BooleanLookup;
import wbs.console.module.ConsoleManager;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.sms.gsm.GsmUtils;
import wbs.sms.messageset.model.MessageSetMessageObjectHelper;
import wbs.sms.messageset.model.MessageSetMessageRec;
import wbs.sms.messageset.model.MessageSetRec;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;

@Accessors (fluent = true)
@Log4j
@PrototypeComponent ("messageSetAction")
public
class MessageSetAction
	extends ConsoleAction {

	@Inject
	Provider<ConsoleManager> consoleManagerProvider;

	@Inject
	Database database;

	@Inject
	EventLogic eventLogic;

	@Getter @Setter
	MessageSetFinder messageSetFinder;

	@Inject
	MessageSetMessageObjectHelper messageSetMessageHelper;

	@Getter @Setter
	BooleanLookup privLookup;

	@Getter @Setter
	Provider<Responder> responder;

	@Inject
	RouteObjectHelper routeHelper;

	@Inject
	UserConsoleLogic userConsoleLogic;

	@Override
	public
	Responder backupResponder () {
		return responder.get ();
	}

	public
	MessageSetAction responderName (
			String responderName) {

		return responder (
			consoleManagerProvider.get ().responder (
				responderName,
				true));

	}

	@Override
	public
	Responder goReal () {

		// check privs

		if (
			! privLookup.lookup (
				requestContext.contextStuff ())
		) {

			requestContext.addError (
				"Access denied");

			return null;

		}

		// get relevant dao objects

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"MessageSetAction.goReal ()",
				this);

		// lookup the message set

		MessageSetRec messageSet =
			messageSetFinder.findMessageSet (
				requestContext);

		// iterate over the input and check them

		long numMessages =
			requestContext.parameterIntegerRequired (
				"num_messages");

		for (
			long index = 0;
			index < numMessages;
			index ++
		) {

			if (
				isNotPresent (
					requestContext.parameter (
						"enabled_" + index))
			) {
				continue;
			}

			if (
				! Pattern.matches (
					"\\d+",
					requestContext.parameterRequired (
						"route_" + index))
			) {

				requestContext.addError (
					stringFormat (
						"Message %s has no route",
						index + 1));

				return null;

			}

			if (
				stringIsEmpty (
					requestContext.parameterRequired (
						"number_" + index))
			) {

				requestContext.addError (
					"Message " + (index + 1) + " has no number");

				return null;

			}

			String message =
				requestContext.parameterRequired (
					"message_" + index);

			if (! GsmUtils.gsmStringIsValid (message)) {

				requestContext.addError (
					"Message " + (index + 1) + " has invalid characters");

				return null;

			}

			if (GsmUtils.gsmStringLength (message) > 160) {

				requestContext.addError (
					"Message " + (index + 1) + " is too long");

				return null;

			}

		}

		// iterate over the input and do it

		for (
			long index = 0;
			index < numMessages;
			index ++
		) {

			log.debug (index);

			boolean enabled =
				isPresent (
					requestContext.parameter (
						"enabled_" + index));

			MessageSetMessageRec messageSetMessage =
				index < messageSet.getMessages ().size ()
					? messageSet.getMessages ().get (
						toJavaIntegerRequired (
							index))
					: null;

			log.debug (
				"enabled " + enabled);

			log.debug (
				"msg " + (messageSetMessage != null));


			if (messageSetMessage != null && ! enabled) {

				log.debug (
					"deleting");

				// delete existing message

				messageSetMessageHelper.remove (
					messageSetMessage);

//				messageSet.getMessages ().remove (
//					new Integer (index));

				eventLogic.createEvent (
					"messageset_message_removed",
					userConsoleLogic.userRequired (),
					index,
					messageSet);

			} else if (enabled) {

				// set up some handy variables

				RouteRec newRoute =
					routeHelper.findRequired (
						requestContext.parameterIntegerRequired (
							"route_" + index));

				String newNumber =
					requestContext.parameterRequired (
						"number_" + index);

				String newMessage =
					requestContext.parameterRequired (
						"message_" + index);

				if (
					isNull (
						messageSetMessage)
				) {

					log.debug (
						"creating");

					// create new message

					messageSetMessage =
						messageSetMessageHelper.createInstance ()

						.setMessageSet (
							messageSet)

						.setIndex (
							index)

						.setRoute (
							newRoute)

						.setNumber (
							newNumber)

						.setMessage (
							newMessage);

					messageSetMessageHelper.insert (
						messageSetMessage);

					messageSet.getMessages ().add (
						messageSetMessage);

					// and create event

					eventLogic.createEvent (
						"messageset_message_created",
						userConsoleLogic.userRequired (),
						index,
						messageSet,
						newRoute,
						newNumber,
						0,
						newMessage);

				} else {

					log.debug (
						"updating");

					// update existing message

					if (
						referenceNotEqualSafe (
							messageSetMessage.getRoute (),
							newRoute)
					) {

						messageSetMessage

							.setRoute (
								newRoute);

						eventLogic.createEvent (
							"messageset_message_route",
							userConsoleLogic.userRequired (),
							index,
							messageSet,
							newRoute);

					}

					if (
						stringNotEqualSafe (
							messageSetMessage.getNumber (),
							newNumber)
					) {

						messageSetMessage

							.setNumber (
								newNumber);

						eventLogic.createEvent (
							"messageset_message_number",
							userConsoleLogic.userRequired (),
							index,
							messageSet,
							newNumber);

					}

					if (
						referenceNotEqualSafe (
							messageSetMessage.getMessage (),
							newMessage)
					) {

						messageSetMessage

							.setMessage (
								newMessage);

						eventLogic.createEvent (
							"messageset_message_message",
							userConsoleLogic.userRequired (),
							index,
							messageSet,
							newMessage);

					}

				}

			}

		}

		transaction.commit ();

		requestContext.addNotice (
			"Messages updated");

		requestContext.setEmptyFormData ();

		return null;

	}

}
