package wbs.sms.message.inbox.console;

import static wbs.framework.utils.etc.Misc.dateToInstant;

import java.util.List;

import javax.inject.Inject;

import wbs.console.helper.ConsoleObjectManager;
import wbs.console.misc.TimeFormatter;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;
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
	TimeFormatter timeFormatter;

	// state

	List<InboxRec> inboxes;

	// implementation

	@Override
	public
	void prepare () {

		inboxes =
			inboxHelper.findPendingLimit (
				1000);

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
				timeFormatter.instantToTimestampString (
					timeFormatter.defaultTimezone (),
					dateToInstant (
						message.getCreatedTime ())));

			printFormat (
				"<td>%h</td>\n",
				inbox.getNumAttempts ());

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
				"<td colspan=\"6\">%h</td>\n",
				message.getText ().getText ());

			printFormat (
				"</tr>\n");

			// status message

			if (inbox.getStatusMessage () != null) {

				printFormat (
					"<tr>\n");

				printFormat (
					"<td colspan=\"6\">%h</td>\n",
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
