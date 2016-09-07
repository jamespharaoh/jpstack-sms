package wbs.sms.message.inbox.console;

import java.util.List;

import javax.inject.Inject;

import wbs.console.helper.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.model.InboxObjectHelper;
import wbs.sms.message.inbox.model.InboxRec;

@PrototypeComponent ("messageInboxSummaryPart")
public
class MessageInboxSummaryPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	InboxObjectHelper inboxHelper;

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	UserConsoleLogic userConsoleLogic;

	// state

	List<InboxRec> inboxes;

	// implementation

	@Override
	public
	void prepare () {

		inboxes =
			inboxHelper.findPendingLimit (
				1000l);

	}

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<form method=\"post\">\n");

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th>Message</th>\n",
			"<th>From</th>\n",
			"<th>To</th>\n",
			"<th>Created</th>\n",
			"<th>Tries</th>\n",
			"<th>Next try</th>\n",
			"<th>Route</th>\n",
			"<th>Actions</th>\n",
			"</tr>\n");

		for (
			InboxRec inbox
				: inboxes
		) {

			printFormat (
				"<tr class=\"sep\">\n",
				"</tr>\n");

			MessageRec message =
				inbox.getMessage ();

			printFormat (
				"<tr>\n");

			printFormat (
				"%s\n",
				objectManager.tdForObjectMiniLink (
					message));

			printFormat (
				"%s\n",
				objectManager.tdForObjectMiniLink (
					message.getNumber ()));

			printFormat (
				"<td>%h</td>\n",
				message.getNumTo ());

			printFormat (
				"<td>%h</td>\n",
				userConsoleLogic.timestampWithoutTimezoneString (
					message.getCreatedTime ()));

			printFormat (
				"<td>%h</td>\n",
				inbox.getNumAttempts ());

			printFormat (
				"<td>%h</td>\n",
				userConsoleLogic.timestampWithoutTimezoneString (
					inbox.getNextAttempt ()));

			printFormat (
				"%s\n",
				objectManager.tdForObjectMiniLink (
					message.getRoute ()));

			printFormat (
				"<td",
				" rowspan=\"%h\"",
				inbox.getStatusMessage () != null ? 3 : 2,
				"><input",
				" type=\"submit\"",
				" name=\"ignore_%h\"",
				message.getId (),
				" value=\"cancel\"",
				"></td>\n");

			printFormat (
				"</tr>\n");

			// message text

			printFormat (
				"<tr>\n");

			printFormat (
				"<td colspan=\"7\">%h</td>\n",
				message.getText ().getText ());

			printFormat (
				"</tr>\n");

			// status message

			if (inbox.getStatusMessage () != null) {

				printFormat (
					"<tr>\n");

				printFormat (
					"<td colspan=\"7\">%h</td>\n",
					inbox.getStatusMessage ());

				printFormat (
					"</tr>\n");

			}

		}

		printFormat (
			"</table>\n");

		printFormat (
			"</form>\n");

	}

}
