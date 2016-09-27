package wbs.sms.message.core.console;

import static wbs.utils.etc.EnumUtils.enumEqualSafe;
import static wbs.utils.etc.EnumUtils.enumInSafe;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphClose;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphWriteFormat;
import static wbs.utils.web.HtmlFormUtils.htmlFormClose;
import static wbs.utils.web.HtmlFormUtils.htmlFormOpenPostAction;

import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;

@PrototypeComponent ("messageActionsPart")
public
class MessageActionsPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	MessageConsoleHelper messageHelper;

	// state

	MessageRec message;

	// implementation

	@Override
	public
	void prepare () {

		message =
			messageHelper.findRequired (
				requestContext.stuffInteger (
					"messageId"));

	}

	@Override
	public
	void renderHtmlBodyContent () {

		htmlFormOpenPostAction (
			requestContext.resolveLocalUrl (
				"/message.actions"));

		if (

			enumEqualSafe (
				message.getDirection (),
				MessageDirection.out)

			&& enumInSafe (
				message.getStatus (),
				MessageStatus.sent,
				MessageStatus.submitted,
				MessageStatus.delivered,
				MessageStatus.manuallyDelivered)

		) {

			htmlParagraphWriteFormat (
				"This outbound message is in the \"%h\" ",
				message.getStatus ().getDescription (),
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
				message.getDirection (),
				MessageDirection.out)

			&& enumInSafe (
				message.getStatus (),
				MessageStatus.undelivered,
				MessageStatus.manuallyUndelivered,
				MessageStatus.reportTimedOut)

		) {

			htmlParagraphWriteFormat (
				"This outbound message is in the \"%h\" ",
				message.getStatus ().getDescription (),
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
				message.getDirection (),
				MessageDirection.out)

			&& enumEqualSafe (
				message.getStatus (),
				MessageStatus.held)

		) {

			htmlParagraphWriteFormat (
				"This outbound message is in the \"%h\" ",
				message.getStatus ().getDescription (),
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
				message.getDirection (),
				MessageDirection.out)

			&& enumInSafe (
				message.getStatus (),
				MessageStatus.failed,
				MessageStatus.cancelled,
				MessageStatus.blacklisted)

		) {

			htmlParagraphWriteFormat (
				"This outbound message is in the \"%h\" ",
				message.getStatus ().getDescription (),
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
