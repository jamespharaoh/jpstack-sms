package wbs.sms.message.inbox.console;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import lombok.Cleanup;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.console.action.ConsoleAction;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.message.inbox.model.InboxObjectHelper;
import wbs.sms.message.inbox.model.InboxRec;

@PrototypeComponent ("messageInboxAction")
public
class MessageInboxAction
	extends ConsoleAction {

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	Database database;

	@Inject
	InboxObjectHelper inboxHelper;

	@Override
	public
	Responder backupResponder () {
		return responder ("messageInboxSummaryResponder");
	}

	@Override
	public
	Responder goReal () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		List<String> notices =
			new ArrayList<String> ();

		for (String paramName
				: requestContext.parameterMap ().keySet ()) {

			Matcher matcher =
				ignorePattern.matcher (
					paramName);

			if (! matcher.matches ())
				continue;

			int messageId =
				Integer.parseInt (
					matcher.group (1));

			InboxRec inbox =
				inboxHelper.find (
					messageId);

			if (inbox == null) {

				requestContext.addError (
					stringFormat (
						"Inbox message %s ",
						messageId,
						"not found"));

				return null;

			}

			MessageRec message =
				inbox.getMessage ();

			message.setStatus (
				MessageStatus.ignored);

			inboxHelper.remove (
				inbox);

			notices.add (
				stringFormat (
					"Removed inbox message %s",
					messageId));

		}

		transaction.commit ();

		for (String notice : notices)
			requestContext.addNotice (notice);

		return null;

	}

	private final static
	Pattern ignorePattern =
		Pattern.compile (
			"ignore_([1-9][0-9]*)");

}
