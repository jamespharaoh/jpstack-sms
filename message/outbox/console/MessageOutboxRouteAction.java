package wbs.sms.message.outbox.console;

import static wbs.utils.etc.EnumUtils.enumNotEqualSafe;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;

import java.util.Collection;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import lombok.NonNull;

import wbs.console.action.ConsoleAction;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.console.UserConsoleLogic;

import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.message.outbox.logic.SmsOutboxLogic;
import wbs.sms.message.outbox.model.OutboxRec;

import wbs.web.responder.Responder;

@PrototypeComponent ("messageOutboxRouteAction")
public
class MessageOutboxRouteAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	Database database;

	@SingletonDependency
	EventLogic eventLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	OutboxConsoleHelper outboxHelper;

	@SingletonDependency
	SmsOutboxLogic outboxLogic;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// details

	@Override
	public
	Responder backupResponder (
			@NonNull TaskLogger parentTaskLogger) {

		return responder (
			"messageOutboxRouteResponder");

	}

	// implementation

	@Override
	public
	Responder goReal (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"goReal");

		) {

			OutboxRec outbox =
				outboxHelper.findRequired (
					transaction,
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

			if (

				! privChecker.canRecursive (
					transaction,
					ImmutableMap.<Object, Collection <String>> builder ()

					.put (
						objectManager.getParentRequired (
							transaction,
							message.getService ()),
						ImmutableSet.of ("manage"))

					.put (
						message.getRoute (),
						ImmutableSet.of ("manage"))

					.build ()

				)

			) {

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
					transaction,
					outbox.getMessage ());

				eventLogic.createEvent (
					transaction,
					"sms_outbox_cancelled",
					userConsoleLogic.userRequired (
						transaction),
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
					transaction,
					outbox.getMessage ());

				eventLogic.createEvent (
					transaction,
					"sms_outbox_retried",
					userConsoleLogic.userRequired (
						transaction),
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

}
