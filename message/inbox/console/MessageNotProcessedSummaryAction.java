package wbs.sms.message.inbox.console;

import javax.inject.Inject;

import lombok.Cleanup;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.console.action.ConsoleAction;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.sms.message.core.logic.MessageLogic;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.message.inbox.model.InboxObjectHelper;
import wbs.sms.message.inbox.model.InboxRec;

@PrototypeComponent ("messageNotProcessedSummaryAction")
public
class MessageNotProcessedSummaryAction
	extends ConsoleAction {

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	Database database;

	@Inject
	InboxObjectHelper inboxHelper;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	MessageLogic messageLogic;

	@Override
	public
	Responder backupResponder () {
		return responder ("messageNotProcessedSummaryResponder");
	}

	@Override
	public
	Responder goReal () {

		int messageId =
			Integer.parseInt (
				requestContext.parameter ("messageId"));

		String notice = null;

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		// load the message

		MessageRec message =
			messageHelper.find (messageId);

		// check the message status is correct

		if (message.getStatus () != MessageStatus.notProcessed) {

			requestContext.addError (
				"Message is not showing as not processed");

			return null;

		}

		if (requestContext.parameter ("process_again") != null) {

			messageLogic.messageStatus (
				message,
				MessageStatus.pending);

			inboxHelper.insert (
				new InboxRec ()
					.setMessage (message));

			notice =
				"Message queued for processing";

		} else if (requestContext.parameter ("ignore") != null) {

			message
				.setStatus (MessageStatus.ignored);

			notice =
				"Message ignored";

		} else if (requestContext.parameter ("processed_manually") != null) {

			message
				.setStatus (MessageStatus.manuallyProcessed);

			notice =
				"Message marked as processed manually";

		}

		if (notice == null) {

			requestContext.addError (
				"Internal error");

			return null;

		}

		transaction.commit ();

		requestContext.addNotice (notice);

		return responder ("messageNotProcessedListResponder");

	}

}
