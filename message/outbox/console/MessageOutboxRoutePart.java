package wbs.sms.message.outbox.console;

import static wbs.framework.utils.etc.StringUtils.stringFormat;

import java.util.Collection;
import java.util.TreeSet;

import javax.inject.Inject;

import wbs.console.helper.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.model.OutboxRec;
import wbs.sms.route.core.console.RouteConsoleHelper;
import wbs.sms.route.core.model.RouteRec;

@PrototypeComponent ("messageOutboxRoutePart")
public
class MessageOutboxRoutePart
	extends AbstractPagePart {

	// dependencies

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	OutboxConsoleHelper outboxHelper;

	@Inject
	RouteConsoleHelper routeHelper;

	@Inject
	UserConsoleLogic userConsoleLogic;

	// state

	RouteRec route;

	Collection<OutboxRec> outboxes;

	@Override
	public
	void prepare () {

		route =
			routeHelper.findRequired (
				requestContext.parameterInteger (
					"routeId"));

		outboxes =
			new TreeSet <> (
				outboxHelper.findLimit (
					route,
					30l));

	}

	@Override
	public
	void renderHtmlBodyContent () {

		if (outboxes.size () == 30) {

			printFormat (
				"<p>Only showing first 30 results.</p>\n");

		}

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th>Id</th>\n",
			"<th>Created</th>\n",
			"<th>Tries</th>\n",
			"<th>From</th>\n",
			"<th>To</th>\n",
			"<th>Actions</th>\n",
			"</tr>\n");

		if (outboxes.size () == 0) {

			printFormat (
				"<tr>\n",
				"<td colspan=\"6\">Nothing to display</td>\n",
				"</tr>");

		}

		for (OutboxRec outbox
				: outboxes) {

			MessageRec message =
				outbox.getMessage ();

			printFormat (
				"<tr class=\"sep\">\n");

			printFormat (
				"<tr>\n",

				"<td>%h</td>\n",
				outbox.getId (),

				"<td>%h</td>\n",
				userConsoleLogic.timestampWithTimezoneString (
					message.getCreatedTime ()),

				"<td>%h</td>\n",
				outbox.getTries ());

			if (message.getDirection () == MessageDirection.in) {

				printFormat (
					"%s\n",
					objectManager.tdForObjectMiniLink (
						message.getNumber ()));

			} else {

				printFormat (
					"<td>%h</td>\n",
					message.getNumFrom ());

			}

			if (message.getDirection () == MessageDirection.out) {

				printFormat (
					"%s\n",
					objectManager.tdForObjectMiniLink (
						message.getNumber ()));

			} else {

				printFormat (
					"<td>%h</td>\n",
					message.getNumTo ());

			}

			int rowSpan =
				outbox.getError () != null ? 3 : 2;

			printFormat (
				"<td rowspan=\"%h\">",
				rowSpan,

				"<form",
				" method=\"post\"",
				" action=\"%h\">\n",
				requestContext.resolveLocalUrl (
					stringFormat (
						"/outbox.route",
						"?routeId=%u",
						route.getId ())),

				"<input",
				" type=\"hidden\"",
				" name=\"messageId\"",
				" value=\"%h\">\n",
				outbox.getId (),

				"<input",
				" type=\"submit\"",
				" name=\"cancel\"",
				" value=\"cancel\">\n",

				"<input",
				" type=\"submit\"",
				" name=\"retry\"",
				" value=\"retry\">\n",

				"</form>",

				"</td>\n",

				"</tr>\n");

			printFormat (
				"<tr>",

				"<td colspan=\"5\">%h</td>\n",
				message.getText (),

				"</tr>\n");

			if (outbox.getError() != null) {

				printFormat (
					"<tr>\n",
					"<td colspan=\"5\">%h</td>\n",
					outbox.getError (),
					"</tr>\n");

			}

		}

		printFormat (
			"</table>\n");

	}

}
