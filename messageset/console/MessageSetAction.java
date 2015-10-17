package wbs.sms.messageset.console;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.stringFormat;

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
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;
import wbs.sms.gsm.Gsm;
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
	ConsoleRequestContext requestContext;

	@Inject
	Provider<ConsoleManager> consoleManagerProvider;

	@Inject
	Database database;

	@Inject
	EventLogic eventLogic;

	@Inject
	MessageSetMessageObjectHelper messageSetMessageHelper;

	@Inject
	RouteObjectHelper routeHelper;

	@Inject
	UserObjectHelper userHelper;

	@Getter @Setter
	Provider<Responder> responder;

	@Getter @Setter
	MessageSetFinder messageSetFinder;

	@Getter @Setter
	BooleanLookup privLookup;

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

		if (! privLookup.lookup (
				requestContext.contextStuff ())) {

			requestContext.addError ("Access denied");

			return null;

		}

		// get relevant dao objects

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		// lookup the message set

		MessageSetRec messageSet =
			messageSetFinder.findMessageSet (
				requestContext);

		// iterate over the input and check them

		int numMessages =
			Integer.parseInt (
				requestContext.parameter ("num_messages"));

		for (int index = 0; index < numMessages; index++) {

			if (requestContext.parameter ("enabled_" + index) == null)
				continue;

			if (! Pattern.matches (
					"\\d+",
					requestContext.parameter ("route_" + index))) {

				requestContext.addError (
					stringFormat (
						"Message %s has no route",
						index + 1));

				return null;

			}

			if (equal (requestContext.parameter ("number_" + index),
					"")) {

				requestContext.addError (
					"Message " + (index + 1) + " has no number");

				return null;

			}

			String message =
				requestContext.parameter ("message_" + index);

			if (message == null)
				throw new NullPointerException();
			if (!Gsm.isGsm(message)) {
				requestContext.addError("Message " + (index + 1)
						+ " has invalid characters");
				return null;
			}
			if (Gsm.length(message) > 160) {
				requestContext.addError("Message " + (index + 1) + " is too long");
				return null;
			}
		}

		// lookup the current user

		UserRec myUser =
			userHelper.find (
				requestContext.userId());

		// iterate over the input and do it

		for (
			int index = 0;
			index < numMessages;
			index ++
		) {

			log.debug (index);

			boolean enabled =
				requestContext.parameter ("enabled_" + index) != null;

			MessageSetMessageRec messageSetMessage =
				(MessageSetMessageRec)
				messageSet.getMessages ().get (
					new Integer (index));

			log.debug ("enabled " + enabled);
			log.debug ("msg " + (messageSetMessage != null));

			if (messageSetMessage != null && ! enabled) {

				log.debug("deleting");

				// delete existing message

				messageSetMessageHelper.remove (
					messageSetMessage);

//				messageSet.getMessages ().remove (
//					new Integer (index));

				eventLogic.createEvent (
					"messageset_message_removed",
					myUser,
					index,
					messageSet);

			} else if (enabled) {

				// set up some handy variables

				RouteRec newRoute =
					routeHelper.find (
						Integer.parseInt (
							requestContext.parameter ("route_" + index)));

				String newNumber =
					requestContext.parameter ("number_" + index);

				String newMessage =
					requestContext.parameter ("message_" + index);

				if (messageSetMessage == null) {

					log.debug("creating");

					// create new message

					messageSetMessage =
						new MessageSetMessageRec ()

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
						myUser,
						index,
						messageSet,
						newRoute,
						newNumber,
						0,
						newMessage);

				} else {

					log.debug("updating");

					// update existing message
					if (messageSetMessage.getRoute() != newRoute) {
						messageSetMessage.setRoute(newRoute);
						eventLogic.createEvent("messageset_message_route",
								myUser, index, messageSet, newRoute);
					}

					if (!messageSetMessage.getNumber().equals(newNumber)) {
						messageSetMessage.setNumber(newNumber);
						eventLogic.createEvent("messageset_message_number",
								myUser, index, messageSet, newNumber);
					}

					if (!messageSetMessage.getMessage().equals(newMessage)) {
						messageSetMessage.setMessage(newMessage);
						eventLogic.createEvent(
								"messageset_message_message", myUser, index,
								messageSet, newMessage);
					}
				}
			}
		}

		transaction.commit();

		requestContext.addNotice("Messages updated");
		requestContext.setEmptyFormData();
		return null;

	}

}
