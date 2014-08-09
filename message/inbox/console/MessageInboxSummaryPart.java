package wbs.sms.message.inbox.console;

import static wbs.framework.utils.etc.Misc.dateToInstant;

import java.util.List;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.misc.TimeFormatter;
import wbs.platform.console.part.AbstractPagePart;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.model.InboxObjectHelper;
import wbs.sms.message.inbox.model.InboxRec;

@PrototypeComponent ("messageInboxSummaryPart")
public
class MessageInboxSummaryPart
	extends AbstractPagePart {

	@Inject
	InboxObjectHelper inboxHelper;

	@Inject
	TimeFormatter timeFormatter;

	List<InboxRec> list;

	@Override
	public
	void prepare () {

		list =
			inboxHelper.findAllLimit (
				1000);

	}

	@Override
	public
	void goBodyStuff () {

		printFormat (
			"<form method=\"post\">\n");

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th>ID</th>\n",
			"<th>From</th>\n",
			"<th>To</th>\n",
			"<th>Time</th>\n",
			"<th>Route</th>\n",
			"<th>Actions</th>\n",
			"</tr>\n");

		for (InboxRec inbox : list) {

			MessageRec message =
				inbox.getMessage ();

			printFormat (
				"<tr>\n",

				"<td>%h</td>\n",
				message.getId (),

				"<td>%h</td>\n",
				message.getNumFrom (),

				"<td>%h</td>\n",
				message.getNumTo (),

				"<td>%h</td>\n",
				timeFormatter.instantToTimestampString (
					dateToInstant (message.getCreatedTime ())),

				"<td>%h</td>\n",
				message.getRoute ().getCode (),

				"<td rowspan=\"2\">\n",

				"<input",
				" type=\"submit\"",
				" name=\"ignore_%h\"",
				message.getId (),
				" value=\"cancel\"",
				">\n",

				"</td>\n",

				"</tr>\n");

			printFormat (
				"<tr>\n",

				"<td colspan=\"5\">%h</td>\n",
				message.getText ().getText (),

				"</tr>\n");

		}

		printFormat (
			"</table>\n");

		printFormat (
			"</form>\n");

	}

}
