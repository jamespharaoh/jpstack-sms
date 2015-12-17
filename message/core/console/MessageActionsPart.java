package wbs.sms.message.core.console;

import static wbs.framework.utils.etc.Misc.equal;
import static wbs.framework.utils.etc.Misc.in;

import javax.inject.Inject;

import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;

@PrototypeComponent ("messageActionsPart")
public
class MessageActionsPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	MessageConsoleHelper messageHelper;

	// state

	MessageRec message;

	// implementation

	@Override
	public
	void prepare () {

		message =
			messageHelper.find (
				requestContext.stuffInt (
					"messageId"));

	}

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<form",
			" method=\"post\"",
			" action=\"%h\"",
			requestContext.resolveLocalUrl (
				"/message.actions"),
			">\n");

		if (

			equal (
				message.getDirection (),
				MessageDirection.out)

			&& in (
				message.getStatus (),
				MessageStatus.sent,
				MessageStatus.submitted,
				MessageStatus.delivered,
				MessageStatus.manuallyDelivered)

		) {

			printFormat (
				"<p>This outbound message is in the \"%h\" ",
				message.getStatus ().getDescription (),
				"state, and can be manually undelivered.</p>\n");

			printFormat (
				"<p><input",
				" type=\"submit\"",
				" name=\"manuallyUndeliver\"",
				" value=\"manually undeliver\"",
				"></p>\n");

		} else if (

			equal (
				message.getDirection (),
				MessageDirection.out)

			&& in (
				message.getStatus (),
				MessageStatus.undelivered,
				MessageStatus.manuallyUndelivered,
				MessageStatus.reportTimedOut)

		) {

			printFormat (
				"<p>This outbound message is in the \"%h\" ",
				message.getStatus ().getDescription (),
				"state, and can be manually delivered.</p>\n");

			printFormat (
				"<p><input",
				" type=\"submit\"",
				" name=\"manuallyDeliver\"",
				" value=\"manually deliver\"",
				"></p>\n");

		} else if (

			equal (
				message.getDirection (),
				MessageDirection.out)

			&& equal (
				message.getStatus (),
				MessageStatus.held)

		) {

			printFormat (
				"<p>This outbound message is in the \"%h\" ",
				message.getStatus ().getDescription (),
				"state, and can be manually unheld.</p>\n");

			printFormat (
				"<p><input",
				" type=\"submit\"",
				" name=\"manuallyUnhold\"",
				" value=\"manually unhold\"",
				"></p>\n");

		} else if (

			equal (
				message.getDirection (),
				MessageDirection.out)

			&& equal (
				message.getStatus (),
				MessageStatus.failed)

		) {

			printFormat (
				"<p>This outbound message is in the \"%h\" ",
				message.getStatus ().getDescription (),
				"state, and can be manually retried.</p>\n");

			printFormat (
				"<p><input",
				" type=\"submit\"",
				" name=\"manuallyRetry\"",
				" value=\"manually retry\"",
				"></p>\n");

		} else {

			printFormat (
				"<p>No actions can be taken on this message at this ",
				"time.</p>\n");

		}

		printFormat (
			"</form>\n");

	}

}
