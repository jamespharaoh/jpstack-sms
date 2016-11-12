package wbs.sms.message.core.console;

import static wbs.utils.etc.EnumUtils.enumEqualSafe;
import static wbs.utils.etc.EnumUtils.enumInSafe;
import static wbs.utils.etc.Misc.isNull;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWriteFormat;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPostAction;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;
import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.message.inbox.console.InboxConsoleHelper;
import wbs.sms.message.inbox.model.InboxRec;
import wbs.sms.message.outbox.console.OutboxConsoleHelper;
import wbs.sms.message.outbox.model.OutboxRec;

@PrototypeComponent ("messageActionsPart")
public
class MessageActionsPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	InboxConsoleHelper smsInboxHelper;

	@SingletonDependency
	MessageConsoleHelper smsMessageHelper;

	@SingletonDependency
	OutboxConsoleHelper smsOutboxHelper;

	// state

	MessageRec smsMessage;

	Optional <InboxRec> smsInbox;
	Optional <OutboxRec> smsOutbox;

	// implementation

	@Override
	public
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		smsMessage =
			smsMessageHelper.findRequired (
				requestContext.stuffInteger (
					"messageId"));

		smsInbox =
			smsInboxHelper.find (
				smsMessage.getId ());

		smsOutbox =
			smsOutboxHelper.find (
				smsMessage.getId ());

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		htmlFormOpenPostAction (
			requestContext.resolveLocalUrl (
				"/message.actions"));

		if (

			enumEqualSafe (
				smsMessage.getDirection (),
				MessageDirection.out)

			&& enumEqualSafe (
				smsMessage.getStatus (),
				MessageStatus.pending)

		) {

			if (
				isNull (
					smsOutbox.get ().getSending ())
			) {

				htmlParagraphWriteFormat (
					"This outbound message is in the \"%h\" ",
					smsMessage.getStatus ().getDescription (),
					"state, but is currently being sent, so no action can be ",
					"taken at this time.");

			} else {

				htmlParagraphWriteFormat (
					"This outbound message is in the \"%h\" ",
					smsMessage.getStatus ().getDescription (),
					"state, and can be manually retried.");

				htmlParagraphOpen ();

				formatWriter.writeLineFormat (
					"<input",
					" type=\"submit\"",
					" name=\"manuallyRetry\"",
					" value=\"manually retry\"",
					">");

				htmlParagraphClose ();

			}

		} else if (

			enumEqualSafe (
				smsMessage.getDirection (),
				MessageDirection.out)

			&& enumInSafe (
				smsMessage.getStatus (),
				MessageStatus.sent,
				MessageStatus.submitted,
				MessageStatus.delivered,
				MessageStatus.manuallyDelivered)

		) {

			htmlParagraphWriteFormat (
				"This outbound message is in the \"%h\" ",
				smsMessage.getStatus ().getDescription (),
				"state, and can be manually undelivered.");

			htmlParagraphOpen ();

			formatWriter.writeLineFormat (
				"<input",
				" type=\"submit\"",
				" name=\"manuallyUndeliver\"",
				" value=\"manually undeliver\"",
				">");

			htmlParagraphClose ();

		} else if (

			enumEqualSafe (
				smsMessage.getDirection (),
				MessageDirection.out)

			&& enumInSafe (
				smsMessage.getStatus (),
				MessageStatus.undelivered,
				MessageStatus.manuallyUndelivered,
				MessageStatus.reportTimedOut)

		) {

			htmlParagraphWriteFormat (
				"This outbound message is in the \"%h\" ",
				smsMessage.getStatus ().getDescription (),
				"state, and can be manually delivered.");

			htmlParagraphOpen ();

			formatWriter.writeLineFormat (
				"<input",
				" type=\"submit\"",
				" name=\"manuallyDeliver\"",
				" value=\"manually deliver\"",
				">");

			htmlParagraphClose ();

		} else if (

			enumEqualSafe (
				smsMessage.getDirection (),
				MessageDirection.out)

			&& enumEqualSafe (
				smsMessage.getStatus (),
				MessageStatus.held)

		) {

			htmlParagraphWriteFormat (
				"This outbound message is in the \"%h\" ",
				smsMessage.getStatus ().getDescription (),
				"state, and can be manually unheld.");

			htmlParagraphOpen ();

			formatWriter.writeLineFormat (
				"<input",
				" type=\"submit\"",
				" name=\"manuallyUnhold\"",
				" value=\"manually unhold\"",
				">");

			htmlParagraphClose ();

		} else if (

			enumEqualSafe (
				smsMessage.getDirection (),
				MessageDirection.out)

			&& enumInSafe (
				smsMessage.getStatus (),
				MessageStatus.failed,
				MessageStatus.cancelled,
				MessageStatus.blacklisted)

		) {

			htmlParagraphWriteFormat (
				"This outbound message is in the \"%h\" ",
				smsMessage.getStatus ().getDescription (),
				"state, and can be manually retried.");

			htmlParagraphOpen ();

			formatWriter.writeLineFormat (
				"<input",
				" type=\"submit\"",
				" name=\"manuallyRetry\"",
				" value=\"manually retry\"",
				">");

			htmlParagraphClose ();

		} else {

			htmlParagraphWriteFormat (
				"No actions can be taken on this message at this time.");

		}

		htmlFormClose ();

	}

}
