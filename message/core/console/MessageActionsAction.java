package wbs.sms.message.core.console;

import static wbs.utils.etc.EnumUtils.enumNotEqualSafe;
import static wbs.utils.etc.EnumUtils.enumNotInSafe;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;

import javax.servlet.ServletException;

import lombok.Cleanup;
import lombok.NonNull;

import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.sms.message.core.logic.SmsMessageLogic;
import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.message.outbox.logic.SmsOutboxLogic;

@PrototypeComponent ("messageActionsAction")
public
class MessageActionsAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	EventLogic eventLogic;

	@SingletonDependency
	MessageConsoleHelper messageHelper;

	@SingletonDependency
	SmsMessageLogic messageLogic;

	@SingletonDependency
	SmsOutboxLogic outboxLogic;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// state

	MessageRec message;

	// details

	@Override
	protected
	Responder backupResponder () {

		return responder (
			"messageActionsResponder");

	}

	// implementation

	@Override
	protected
	Responder goReal ()
		throws ServletException {

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"MessageActionsAction.goReal ()",
				this);

		// load data

		message =
			messageHelper.findRequired (
				requestContext.stuffInteger (
					"messageId"));

		// hand off to appropriate method

		if (
			optionalIsPresent (
				requestContext.parameter (
					"manuallyUndeliver"))
		) {

			return manuallyUndeliver (
				transaction);

		} else if (
			optionalIsPresent (
				requestContext.parameter (
					"manuallyDeliver"))
		) {

			return manuallyDeliver (
				transaction);

		} else if (
			optionalIsPresent (
				requestContext.parameter (
					"manuallyUnhold"))
		) {

			return manuallyUnhold (
				transaction);

		} else if (
			optionalIsPresent (
				requestContext.parameter (
					"manuallyRetry"))
		) {

			return manuallyRetry (
				transaction);

		} else {

			throw new RuntimeException ();

		}

	}

	private
	Responder manuallyUndeliver (
			@NonNull Transaction transaction) {

		if (
			enumNotEqualSafe (
				message.getDirection (),
				MessageDirection.out)
		) {
			throw new RuntimeException ();
		}

		if (
			enumNotInSafe (
				message.getStatus (),
				MessageStatus.sent,
				MessageStatus.submitted,
				MessageStatus.delivered)
		) {

			requestContext.addError (
				"Message in invalid state for this operation");

			return null;

		}

		messageLogic.messageStatus (
			message,
			MessageStatus.manuallyUndelivered);

		eventLogic.createEvent (
			"message_manually_undelivered",
			userConsoleLogic.userRequired (),
			message);

		transaction.commit ();

		requestContext.addNotice (
			"Message manually undelivered");

		return null;

	}

	private
	Responder manuallyDeliver (
			@NonNull Transaction transaction) {

		if (
			enumNotEqualSafe (
				message.getDirection (),
				MessageDirection.out)
		) {
			throw new RuntimeException ();
		}

		if (
			enumNotInSafe (
				message.getStatus (),
				MessageStatus.undelivered,
				MessageStatus.reportTimedOut,
				MessageStatus.manuallyUndelivered)
		) {

			requestContext.addError (
				"Message in invalid state for this operation");

			return null;

		}

		messageLogic.messageStatus (
			message,
			MessageStatus.manuallyDelivered);

		eventLogic.createEvent (
			"message_manually_delivered",
			userConsoleLogic.userRequired (),
			message);

		transaction.commit ();

		requestContext.addNotice (
			"Message manually undelivered");

		return null;

	}

	private
	Responder manuallyUnhold (
			@NonNull Transaction transaction) {

		if (
			enumNotEqualSafe (
				message.getStatus (),
				MessageStatus.held)
		) {

			requestContext.addError (
				"Message in invalid state for this operation");

			return null;

		}

		outboxLogic.unholdMessage (
			message);

		eventLogic.createEvent (
			"message_manually_unheld",
			userConsoleLogic.userRequired (),
			message);

		transaction.commit ();

		requestContext.addNotice (
			"Message manually unheld");

		return null;

	}

	private
	Responder manuallyRetry (
			@NonNull Transaction transaction) {

		if (
			enumNotInSafe (
				message.getStatus (),
				MessageStatus.failed,
				MessageStatus.cancelled,
				MessageStatus.blacklisted)
		) {

			requestContext.addError (
				"Message in invalid state for this operation");

			return null;

		}

		outboxLogic.retryMessage (
			message);

		eventLogic.createEvent (
			"message_manually_retried",
			userConsoleLogic.userRequired (),
			message);

		transaction.commit ();

		requestContext.addNotice (
			"Message manually retried");

		return null;

	}

}
