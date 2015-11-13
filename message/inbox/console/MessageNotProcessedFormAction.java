package wbs.sms.message.inbox.console;

import javax.inject.Inject;

import lombok.Cleanup;

import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.queue.logic.QueueLogic;
import wbs.platform.user.console.UserConsoleHelper;
import wbs.platform.user.model.UserRec;
import wbs.sms.message.core.console.MessageConsoleHelper;
import wbs.sms.message.core.logic.MessageLogic;
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
	InboxConsoleHelper inboxHelper;

	@Inject
	MessageConsoleHelper messageHelper;

	@Inject
	MessageLogic messageLogic;

	@Inject
	QueueLogic queueLogic;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	UserConsoleHelper userHelper;

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
				this);

		MessageRec message =
			messageHelper.find (
				requestContext.stuffInt (
					"messageId"));

		UserRec myUser =
			userHelper.find (
				requestContext.userId ());

		// check the message status is correct

		if (message.getStatus () != MessageStatus.notProcessed) {

			requestContext.addError (
				"Message is not in correct state");

			return responder (
				"queueHomeResponder");

		}

		if (requestContext.parameter ("process_again") != null) {

			queueLogic.processQueueItem (
				message.getNotProcessedQueueItem (),
				myUser);

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

			transaction.commit ();

			requestContext.addNotice (
				"Message queued for processing");

			return responder (
				"queueHomeResponder");

		}

		if (requestContext.parameter ("ignore") != null) {

			queueLogic.processQueueItem (
				message.getNotProcessedQueueItem (),
				myUser);

			messageLogic.messageStatus (
				message,
				MessageStatus.ignored);

			message

				.setNotProcessedQueueItem (
					null);

			transaction.commit ();

			requestContext.addNotice (
				"Message ignored");

			return responder (
				"queueHomeResponder");

		}

		if (requestContext.parameter ("processed_manually") != null) {

			queueLogic.processQueueItem (
				message.getNotProcessedQueueItem (),
				myUser);

			messageLogic.messageStatus (
				message,
				MessageStatus.manuallyProcessed);

			message

				.setNotProcessedQueueItem (
					null);

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
