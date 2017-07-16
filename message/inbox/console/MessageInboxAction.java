package wbs.sms.message.inbox.console;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.NonNull;

import wbs.console.action.ConsoleAction;
import wbs.console.notice.ConsoleNotices;
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

import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.message.inbox.model.InboxObjectHelper;
import wbs.sms.message.inbox.model.InboxRec;
import wbs.sms.message.inbox.model.InboxState;

import wbs.web.responder.WebResponder;

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

	// prototype dependencies

	@PrototypeDependency
	@NamedDependency ("messageInboxSummaryResponder")
	ComponentProvider <WebResponder> summaryResponderProvider;

	// details

	@Override
	public
	WebResponder backupResponder (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"backupResponder");

		) {

			return summaryResponderProvider.provide (
				taskLogger);

		}

	}

	@Override
	public
	WebResponder goReal (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"goReal");

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
						transaction,
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
