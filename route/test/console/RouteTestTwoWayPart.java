package wbs.sms.route.test.console;

import java.util.Collection;

import javax.inject.Inject;

import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.sms.message.core.console.MessageConsoleHelper;
import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageSearch;
import wbs.sms.message.core.model.MessageSearch.MessageSearchOrder;

@PrototypeComponent ("routeTestTwoWayPart")
public
class RouteTestTwoWayPart
	extends AbstractPagePart {

	@Inject
	MessageConsoleHelper messageHelper;

	Collection<MessageRec> messages;

	@Override
	public
	void prepare () {

		String number =
			requestContext.parameterOrNull ("num_from");

		if (number == null)
			return;

		MessageSearch search =
			new MessageSearch ()
				.number (number)
				.maxResults (20)
				.orderBy (MessageSearchOrder.createdTimeDesc);

		messages =
			messageHelper.search (
				search);

	}

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<form",
			" action=\"%h\"",
			requestContext.resolveLocalUrl (
				"/route.test.twoWay"),
			" method=\"post\"",
			">\n");

		printFormat (
			"<table class=\"details\">\n");

		String str =
			requestContext.parameterOrNull ("num_from");

		if (str == null)
			str = "";

		printFormat (
			"<tr>\n",

			"<th>Num from</th>\n",

			"<td><input",
			" type=\"text\"",
			" name=\"num_from\"",
			" size=\"32\"",
			" value=\"%h\"",
			str,
			"></td>\n",

			"</tr>\n");

		str =
			requestContext.parameterOrNull ("num_to");

		if (str == null)
			str = "";

		printFormat (
			"<tr>\n",

			"<th>Num to</th>\n",

			"<td><input",
			" type=\"text\"",
			" name=\"num_to\"",
			" size=\"32\"",
			" value=\"%h\"",
			str,
			"></td>\n",

			"</tr>\n");

		printFormat (
			"<tr>\n",

			"<th>Message</th>\n",

			"<td><textarea",
			" name=\"message\"",
			" rows=\"3\"",
			" cols=\"64\"",
			"></textarea></td>\n",

			"</tr>\n");

		printFormat (
			"</table>\n");

		printFormat (
			"<p>",
			"<input",
			" type=\"submit\"",
			" value=\"send message\"",
			"></p>\n");

		if (messages != null && messages.size () > 0) {

			printFormat (
				"<h3>Messages</h3>\n");

			printFormat (
				"<table class=\"list\">\n");

			printFormat (
				"<tr>\n",
				"<th>Number</th>\n",
				"<th>Message</th>\n",
				"</tr>\n");

			for (MessageRec message
					: messages) {

				printFormat (
					"<tr>\n",
					"<td>%h</td>\n",
					message.getDirection () == MessageDirection.in
						? message.getNumTo ()
						: message.getNumFrom (),
					"<td>%h</td>\n",
					message.getText (),
					"</tr>\n");

			}

			printFormat (
				"</table>\n");

		}

		printFormat (
			"</form>\n");

	}

}
