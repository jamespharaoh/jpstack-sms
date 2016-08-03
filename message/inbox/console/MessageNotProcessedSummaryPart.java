package wbs.sms.message.inbox.console;

import static wbs.framework.utils.etc.StringUtils.emptyStringIfNull;

import javax.inject.Inject;

import wbs.console.helper.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.sms.message.core.console.MessageConsoleLogic;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;

@PrototypeComponent ("messageNotProcessedSummaryPart")
public
class MessageNotProcessedSummaryPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	MessageConsoleLogic messageConsoleLogic;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	UserConsoleLogic userConsoleLogic;

	// state

	MessageRec message;

	// implementation

	@Override
	public
	void prepare () {

		message =
			messageHelper.findRequired (
				requestContext.stuffInt (
					"messageId"));

	}

	@Override
	public
	void renderHtmlBodyContent () {

		if (message.getStatus () != MessageStatus.notProcessed) {

			printFormat (
				"<p>Message is not in correct state</p>\n");

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
			objectManager.tdForObjectMiniLink (
				message.getRoute ()),

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Status</th>\n",

			"%s\n",
			messageConsoleLogic.tdForMessageStatus (
				message.getStatus ()),

			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Time sent</th>\n",
			"<td>%h</td>\n",
			message.getNetworkTime () != null
				? userConsoleLogic.timestampWithTimezoneString (
					message.getNetworkTime ())
				: "-",
			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Time received</th>\n",
			"<td>%h</td>\n",
			userConsoleLogic.timestampWithTimezoneString (
				message.getCreatedTime ()),
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
			emptyStringIfNull (
				message.getAdultVerified ()),

			"</tr>\n");

		/*
		printFormat (
			"<tr>\n",
			"<th>Notes</th>\n",

			"<td>%h</td>\n",
			emptyStringIfNull (
				message.getNotes ()),

			"</tr>");
		*/

		printFormat (
			"</table>");

	}

}
