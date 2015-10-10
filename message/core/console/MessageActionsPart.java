package wbs.sms.message.core.console;

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

		if (
			message.getDirection () == MessageDirection.out
			&& in (message.getStatus (),
				MessageStatus.sent,
				MessageStatus.submitted,
				MessageStatus.delivered)
		) {

			printFormat (
				"<form",
				" method=\"post\"",
				" action=\"%h\"",
				requestContext.resolveLocalUrl (
					"/message.actions"),
				">\n");

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

			printFormat (
				"</form>\n");

		} else {

			printFormat (
				"<p>No actions can be taken on this message at this ",
				"time.</p>\n");

		}

	}

}
