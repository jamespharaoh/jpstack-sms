package wbs.sms.message.inbox.console;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.NonNull;

import wbs.console.action.ConsoleAction;
import wbs.console.notice.ConsoleNotices;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.message.inbox.model.InboxObjectHelper;
import wbs.sms.message.inbox.model.InboxRec;
import wbs.sms.message.inbox.model.InboxState;

import wbs.web.responder.Responder;

@PrototypeComponent ("messageInboxAction")
public
class MessageInboxAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	Database database;

	@SingletonDependency
	InboxObjectHelper inboxHelper;

	@ClassSingletonDependency
	LogContext logContext;

	// details

	@Override
	public
	Responder backupResponder (
			@NonNull TaskLogger parentTaskLogger) {

		return responder (
			"messageInboxSummaryResponder");

	}

	@Override
	public
	Responder goReal (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"goReal");

		try (

			Transaction transaction =
				database.beginReadWrite (
					taskLogger,
					"MessageInboxAction.goReal ()",
					this);

		) {

			ConsoleNotices notices =
				new ConsoleNotices ();

			for (
				String paramName
					: requestContext.parameterMap ().keySet ()
			) {

				Matcher matcher =
					ignorePattern.matcher (
						paramName);

				if (! matcher.matches ())
					continue;

				Long messageId =
					Long.parseLong (
						matcher.group (
							1));

				InboxRec inbox =
					inboxHelper.findRequired (
						messageId);

				MessageRec message =
					inbox.getMessage ();

				// check state

				if (inbox.getState () != InboxState.pending) {

					requestContext.addErrorFormat (
						"Inbox message %s ",
						integerToDecimalString (
							messageId),
						"is not pending");

				}

				// update inbox

				inbox

					.setState (
						InboxState.ignored);

				// update message

				message

					.setStatus (
						MessageStatus.ignored);

				notices.noticeFormat (
					"Ignored inbox message %s",
					integerToDecimalString (
						messageId));

			}

			transaction.commit ();

			requestContext.addNotices (
				notices);

			return null;

		}

	}

	private final static
	Pattern ignorePattern =
		Pattern.compile (
			"ignore_([1-9][0-9]*)");

}
