package wbs.sms.message.outbox.console;

import static wbs.framework.utils.etc.Misc.isNotNull;
import static wbs.framework.utils.etc.Misc.notEqual;

import java.util.Collection;

import javax.inject.Inject;

import lombok.Cleanup;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.console.action.ConsoleAction;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.priv.console.PrivChecker;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserRec;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.message.outbox.logic.OutboxLogic;
import wbs.sms.message.outbox.model.OutboxRec;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

@PrototypeComponent ("messageOutboxRouteAction")
public
class MessageOutboxRouteAction
	extends ConsoleAction {

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	Database database;

	@Inject
	EventLogic eventLogic;

	@Inject
	OutboxConsoleHelper outboxHelper;

	@Inject
	OutboxLogic outboxLogic;

	@Inject
	PrivChecker privChecker;

	@Inject
	UserObjectHelper userHelper;

	@Override
	public
	Responder backupResponder () {
		return responder ("messageOutboxRouteResponder");
	}

	@Override
	public
	Responder goReal () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		UserRec myUser =
			userHelper.find (
				requestContext.userId ());

		OutboxRec outbox =
			outboxHelper.find (
				Integer.parseInt (
					requestContext.parameter (
						"messageId")));

		if (outbox == null) {

			requestContext.addError (
				"Outbox not found");

			return null;

		}

		MessageRec message =
			outbox.getMessage ();

		// check privs

		if (! privChecker.can (
			ImmutableMap.<Object,Collection<String>>builder ()

				.put (
					objectManager.getParent (
						message.getService ()),
					ImmutableSet.<String>of ("manage"))

				.put (
					message.getRoute (),
					ImmutableSet.<String>of ("manage"))

				.build ())) {

			requestContext.addError (
				"Access denied");

			return null;

		}

		// check message state

		if (
			notEqual (
				message.getStatus (),
				MessageStatus.pending)
		) {

			requestContext.addError (
				"Message is not pending");

			return null;

		}

		// check message is not being sent

		if (
			isNotNull (
				outbox.getSending ())
		) {

			requestContext.addError (
				"Message is being sent");

			return null;

		}

		if (
			isNotNull (
				requestContext.parameter (
					"cancel"))
		) {

			// cancel message

			outboxLogic.cancelMessage (
				outbox.getMessage ());

			eventLogic.createEvent (
				"sms_outbox_cancelled",
				myUser,
				outbox.getMessage ());

			transaction.commit ();

			requestContext.addNotice (
				"Message cancelled");

		} else if (
			isNotNull (
				requestContext.parameter (
					"retry"))
		) {

			// retry message

			outboxLogic.retryMessage (
				outbox.getMessage ());

			eventLogic.createEvent (
				"sms_outbox_retried",
				myUser,
				outbox.getMessage ());

			transaction.commit ();

			requestContext.addNotice (
				"Message retried");

		} else {

			throw new RuntimeException ();

		}

		return null;

	}

}
