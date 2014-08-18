package wbs.sms.message.inbox.console;

import static wbs.framework.utils.etc.Misc.dateToInstant;
import static wbs.framework.utils.etc.Misc.stringFormat;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.console.misc.TimeFormatter;
import wbs.platform.console.part.AbstractPagePart;
import wbs.sms.message.core.console.MessageConsoleStuff;
import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;

@PrototypeComponent ("messageNotProcessedSummaryPart")
public
class MessageNotProcessedSummaryPart
	extends AbstractPagePart {

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	TimeFormatter timeFormatter;

	MessageRec message;

	@Override
	public
	void prepare () {

		String messageIdParam =
			requestContext.parameter ("messageId");

		int messageId =
			Integer.parseInt (messageIdParam);

		message =
			messageHelper.find (
				messageId);

		if (message != null
				&& message.getDirection () != MessageDirection.in)
			message = null;

	}

	@Override
	public
	void goBodyStuff () {

		if (message == null) {

			printFormat (
				"<p>Not found</p>\n");

			return;

		}

		printFormat (
			"<table class=\"details\">");

		printFormat (
			"<tr>\n",
			"<th>ID</th>\n",

			"<td>%h</td>\n",
			message.getId (),

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>From</th>\n",

			"<td>%h</td>\n",
			message.getNumFrom (),
			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>To</th>\n",

			"<td>%h</td>\n",
			message.getNumTo (),

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Message</th>\n",

			"<td>%h</td>\n",
			message.getText ().getText (),

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Route</th>\n",

			"%s\n",
			objectManager.tdForObject (
				message.getRoute (),
				null,
				true,
				true),

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Status</th>\n",

			"%s\n",
			MessageConsoleStuff.tdForMessageStatus (
				message.getStatus ()),

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Time sent</th>\n",

			"<td>%h</td>\n",
			message.getNetworkTime () != null
				? timeFormatter.instantToTimestampString (
					timeFormatter.defaultTimezone (),
					dateToInstant (
						message.getNetworkTime ()))
				: "-",

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Time received</th>\n",

			"<td>%h</td>\n",
			timeFormatter.instantToTimestampString (
				timeFormatter.defaultTimezone (),
				dateToInstant (
					message.getCreatedTime ())),

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Charge</th>\n",

			"<td>%h</td>\n",
			message.getCharge (),

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>AVSTATUS</th>\n",

			"<td>%h</td>\n",
			message.getAdultVerified (),

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Notes</th>\n",

			"<td>%h</td>\n",
			message.getNotes (),

			"</tr>");

		printFormat (
			"</table>");

		printFormat (
			"<form",
			" method=\"post\"",
			" action=\"%h\"",
			requestContext.resolveLocalUrl (
				stringFormat (
					"/messageNotProcessed.summary",
					"?messageId=%u",
					message.getId ())),
			">\n",

			"<p><input",
			" type=\"submit\"",
			" name=\"process_again\"",
			" value=\"process again\"",
			">\n",

			"<input",
			" type=\"submit\"",
			" name=\"ignore\"",
			" value=\"ignore\"",
			">\n",

			"<input",
			" type=\"submit\"",
			" name=\"processed_manually\"",
			" value=\"processed manually\"",
			"></p>\n",

			"</form>\n");

	}

}
