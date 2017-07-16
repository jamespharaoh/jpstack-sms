package wbs.sms.message.inbox.console;

import static wbs.utils.etc.Misc.shouldNeverHappen;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;

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
import wbs.platform.queue.console.QueueHomeResponder;
import wbs.platform.queue.logic.QueueLogic;
import wbs.platform.user.console.UserConsoleLogic;

import wbs.sms.message.core.console.MessageConsoleHelper;
import wbs.sms.message.core.logic.SmsMessageLogic;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;

import wbs.web.responder.WebResponder;

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

	// prototype dependencies

	@PrototypeDependency
	@NamedDependency ("messageNotProcessedFormResponder")
	ComponentProvider <WebResponder> formResponderProvider;

	@PrototypeDependency
	ComponentProvider <QueueHomeResponder> queueHomeResponderProvider;

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

			return formResponderProvider.provide (
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

			MessageRec message =
				messageHelper.findFromContextRequired (
					transaction);

			// check the message status is correct

			if (message.getStatus () != MessageStatus.notProcessed) {

				requestContext.addError (
					"Message is not in correct state");

				return queueHomeResponderProvider.provide (
					transaction);

			}

			if (
				optionalIsPresent (
					requestContext.parameter (
						"process_again"))
			) {

				queueLogic.processQueueItem (
					transaction,
					message.getNotProcessedQueueItem (),
					userConsoleLogic.userRequired (
						transaction));

				messageLogic.messageStatus (
					transaction,
					message,
					MessageStatus.pending);

				message

					.setNotProcessedQueueItem (
						null);

				inboxHelper.insert (
					transaction,
					inboxHelper.createInstance ()

					.setMessage (
						message)

				);

				eventLogic.createEvent (
					transaction,
					"message_processed_again",
					userConsoleLogic.userRequired (
						transaction),
					message);

				transaction.commit ();

				requestContext.addNotice (
					"Message queued for processing");

				return queueHomeResponderProvider.provide (
					transaction);

			}

			if (
				optionalIsPresent (
					requestContext.parameter (
						"ignore"))
			) {

				queueLogic.processQueueItem (
					transaction,
					message.getNotProcessedQueueItem (),
					userConsoleLogic.userRequired (
						transaction));

				messageLogic.messageStatus (
					transaction,
					message,
					MessageStatus.ignored);

				message

					.setNotProcessedQueueItem (
						null);

				eventLogic.createEvent (
					transaction,
					"message_ignored",
					userConsoleLogic.userRequired (
						transaction),
					message);

				transaction.commit ();

				requestContext.addNotice (
					"Message ignored");

				return queueHomeResponderProvider.provide (
					transaction);

			}

			if (
				optionalIsPresent (
					requestContext.parameter (
						"processed_manually"))
			) {

				queueLogic.processQueueItem (
					transaction,
					message.getNotProcessedQueueItem (),
					userConsoleLogic.userRequired (
						transaction));

				messageLogic.messageStatus (
					transaction,
					message,
					MessageStatus.manuallyProcessed);

				message

					.setNotProcessedQueueItem (
						null);

				eventLogic.createEvent (
					transaction,
					"message_manually_processed",
					userConsoleLogic.userRequired (
						transaction),
					message);

				transaction.commit ();

				requestContext.addNotice (
					"Message marked as processed manually");

				return queueHomeResponderProvider.provide (
					transaction);

			}

			throw shouldNeverHappen ();

		}

	}

}
