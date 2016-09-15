package wbs.sms.message.outbox.console;

import static wbs.utils.etc.EnumUtils.enumNotEqualSafe;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;

import java.util.Collection;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import lombok.Cleanup;

import wbs.console.action.ConsoleAction;
import wbs.console.helper.ConsoleObjectManager;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.message.outbox.logic.SmsOutboxLogic;
import wbs.sms.message.outbox.model.OutboxRec;

@PrototypeComponent ("messageOutboxRouteAction")
public
class MessageOutboxRouteAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	Database database;

	@SingletonDependency
	EventLogic eventLogic;

	@SingletonDependency
	OutboxConsoleHelper outboxHelper;

	@SingletonDependency
	SmsOutboxLogic outboxLogic;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// details

	@Override
	public
	Responder backupResponder () {
		return responder ("messageOutboxRouteResponder");
	}

	// implementation

	@Override
	public
	Responder goReal () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"MessageOutboxRouteAction.goReal ()",
				this);

		OutboxRec outbox =
			outboxHelper.findRequired (
				requestContext.parameterIntegerRequired (
					"messageId"));

		if (outbox == null) {

			requestContext.addError (
				"Outbox not found");

			return null;

		}

		MessageRec message =
			outbox.getMessage ();

		// check privs

		if (! privChecker.canRecursive (
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
			enumNotEqualSafe (
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
			optionalIsPresent (
				requestContext.parameter (
					"cancel"))
		) {

			// cancel message

			outboxLogic.cancelMessage (
				outbox.getMessage ());

			eventLogic.createEvent (
				"sms_outbox_cancelled",
				userConsoleLogic.userRequired (),
				outbox.getMessage ());

			transaction.commit ();

			requestContext.addNotice (
				"Message cancelled");

		} else if (
			optionalIsPresent (
				requestContext.parameter (
					"retry"))
		) {

			// retry message

			outboxLogic.retryMessage (
				outbox.getMessage ());

			eventLogic.createEvent (
				"sms_outbox_retried",
				userConsoleLogic.userRequired (),
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
