package wbs.sms.message.inbox.console;

import javax.inject.Inject;

import lombok.Cleanup;

import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import static wbs.framework.utils.etc.OptionalUtils.isPresent;
import wbs.framework.web.Responder;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.queue.logic.QueueLogic;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.sms.message.core.console.MessageConsoleHelper;
import wbs.sms.message.core.logic.SmsMessageLogic;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;

@PrototypeComponent ("messageNotProcessedFormAction")
public
class MessageNotProcessedFormAction
	extends ConsoleAction {

	// dependencies

	@Inject
	Database database;

	@Inject
	EventLogic eventLogic;

	@Inject
	InboxConsoleHelper inboxHelper;

	@Inject
	MessageConsoleHelper messageHelper;

	@Inject
	SmsMessageLogic messageLogic;

	@Inject
	QueueLogic queueLogic;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	UserConsoleLogic userConsoleLogic;

	// details

	@Override
	protected
	Responder backupResponder () {

		return responder (
			"messageNotProcessedFormResponder");

	}

	// implementation

	@Override
	protected
	Responder goReal () {

		// begin transaction

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"MessageNotProcessedFormAction.goReal ()",
				this);

		MessageRec message =
			messageHelper.findRequired (
				requestContext.stuffInt (
					"messageId"));

		// check the message status is correct

		if (message.getStatus () != MessageStatus.notProcessed) {

			requestContext.addError (
				"Message is not in correct state");

			return responder (
				"queueHomeResponder");

		}

		if (
			isPresent (
				requestContext.parameter (
					"process_again"))
		) {

			queueLogic.processQueueItem (
				message.getNotProcessedQueueItem (),
				userConsoleLogic.userRequired ());

			messageLogic.messageStatus (
				message,
				MessageStatus.pending);

			message

				.setNotProcessedQueueItem (
					null);

			inboxHelper.insert (
				inboxHelper.createInstance ()

				.setMessage (
					message)

			);

			eventLogic.createEvent (
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
			isPresent (
				requestContext.parameter (
					"ignore"))
		) {

			queueLogic.processQueueItem (
				message.getNotProcessedQueueItem (),
				userConsoleLogic.userRequired ());

			messageLogic.messageStatus (
				message,
				MessageStatus.ignored);

			message

				.setNotProcessedQueueItem (
					null);

			eventLogic.createEvent (
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
			isPresent (
				requestContext.parameter (
					"processed_manually"))
		) {

			queueLogic.processQueueItem (
				message.getNotProcessedQueueItem (),
				userConsoleLogic.userRequired ());

			messageLogic.messageStatus (
				message,
				MessageStatus.manuallyProcessed);

			message

				.setNotProcessedQueueItem (
					null);

			eventLogic.createEvent (
				"message_manually_processed",
				userConsoleLogic.userRequired (),
				message);

			transaction.commit ();

			requestContext.addNotice (
				"Message marked as processed manually");

			return responder (
				"queueHomeResponder");

		}

		throw new RuntimeException (
			"Assertion failure");

	}

}
