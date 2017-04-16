package wbs.sms.message.inbox.console;

import static wbs.utils.etc.Misc.shouldNeverHappen;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;

import lombok.NonNull;

import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.event.logic.EventLogic;
import wbs.platform.queue.logic.QueueLogic;
import wbs.platform.user.console.UserConsoleLogic;

import wbs.sms.message.core.console.MessageConsoleHelper;
import wbs.sms.message.core.logic.SmsMessageLogic;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;

import wbs.web.responder.Responder;

@PrototypeComponent ("messageNotProcessedFormAction")
public
class MessageNotProcessedFormAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	EventLogic eventLogic;

	@SingletonDependency
	InboxConsoleHelper inboxHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MessageConsoleHelper messageHelper;

	@SingletonDependency
	SmsMessageLogic messageLogic;

	@SingletonDependency
	QueueLogic queueLogic;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// details

	@Override
	protected
	Responder backupResponder (
			@NonNull TaskLogger parentTaskLogger) {

		return responder (
			"messageNotProcessedFormResponder");

	}

	// implementation

	@Override
	protected
	Responder goReal (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"goReal");

		// begin transaction

		try (

			Transaction transaction =
				database.beginReadWrite (
					taskLogger,
					"MessageNotProcessedFormAction.goReal ()",
					this);

		) {

			MessageRec message =
				messageHelper.findFromContextRequired ();

			// check the message status is correct

			if (message.getStatus () != MessageStatus.notProcessed) {

				requestContext.addError (
					"Message is not in correct state");

				return responder (
					"queueHomeResponder");

			}

			if (
				optionalIsPresent (
					requestContext.parameter (
						"process_again"))
			) {

				queueLogic.processQueueItem (
					message.getNotProcessedQueueItem (),
					userConsoleLogic.userRequired ());

				messageLogic.messageStatus (
					taskLogger,
					message,
					MessageStatus.pending);

				message

					.setNotProcessedQueueItem (
						null);

				inboxHelper.insert (
					taskLogger,
					inboxHelper.createInstance ()

					.setMessage (
						message)

				);

				eventLogic.createEvent (
					taskLogger,
					"message_processed_again",
					userConsoleLogic.userRequired (),
					message);

				transaction.commit ();

				requestContext.addNotice (
					"Message queued for processing");

				return responder (
					"queueHomeResponder");

			}

			if (
				optionalIsPresent (
					requestContext.parameter (
						"ignore"))
			) {

				queueLogic.processQueueItem (
					message.getNotProcessedQueueItem (),
					userConsoleLogic.userRequired ());

				messageLogic.messageStatus (
					taskLogger,
					message,
					MessageStatus.ignored);

				message

					.setNotProcessedQueueItem (
						null);

				eventLogic.createEvent (
					taskLogger,
					"message_ignored",
					userConsoleLogic.userRequired (),
					message);

				transaction.commit ();

				requestContext.addNotice (
					"Message ignored");

				return responder (
					"queueHomeResponder");

			}

			if (
				optionalIsPresent (
					requestContext.parameter (
						"processed_manually"))
			) {

				queueLogic.processQueueItem (
					message.getNotProcessedQueueItem (),
					userConsoleLogic.userRequired ());

				messageLogic.messageStatus (
					taskLogger,
					message,
					MessageStatus.manuallyProcessed);

				message

					.setNotProcessedQueueItem (
						null);

				eventLogic.createEvent (
					taskLogger,
					"message_manually_processed",
					userConsoleLogic.userRequired (),
					message);

				transaction.commit ();

				requestContext.addNotice (
					"Message marked as processed manually");

				return responder (
					"queueHomeResponder");

			}

			throw shouldNeverHappen ();

		}

	}

}
