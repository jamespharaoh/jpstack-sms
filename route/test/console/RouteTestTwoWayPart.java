package wbs.sms.route.test.console;

import static wbs.framework.utils.etc.OptionalUtils.optionalIsNotPresent;

import java.util.Collection;

import javax.inject.Inject;

import com.google.common.base.Optional;

import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.sms.message.core.console.MessageConsoleHelper;
import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageSearch;
import wbs.sms.message.core.model.MessageSearch.MessageSearchOrder;
import wbs.sms.route.core.console.RouteConsoleHelper;
import wbs.sms.route.core.model.RouteRec;

@PrototypeComponent ("routeTestTwoWayPart")
public
class RouteTestTwoWayPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	MessageConsoleHelper messageHelper;

	@Inject
	RouteConsoleHelper routeHelper;

	// state

	RouteRec route;

	Collection<MessageRec> messages;

	// implementation

	@Override
	public
	void prepare () {

		route =
			routeHelper.findRequired (
				requestContext.stuffInteger (
					"routeId"));

		Optional<String> numberOptional =
			requestContext.parameter (
				"num_from");

		if (
			optionalIsNotPresent (
				numberOptional)
		) {
			return;
		}

		MessageSearch search =
			new MessageSearch ()

			.number (
				numberOptional.get ())

			.maxResults (
				20l)

			.orderBy (
				MessageSearchOrder.createdTimeDesc);

		messages =
			messageHelper.search (
				search);

	}

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<p>This facility can be used to insert an inbound message into ",
			"the system, which will then be treated exactly as if we had ",
			"received it from the aggregator. It will also show messages sent ",
			"back out by the system, allowing an interaction over several ",
			"messages in and out.</p>\n");

		printFormat (
			"<p class=\"warning\">Please note, that this is intended ",
			"primarily for testing, and any other usage should instead be ",
			"performed using a separate facility designed for that specific ",
			"purpose.\n");

		if (! route.getCanReceive ()) {

			printFormat (
				"<p class=\"error\">This route is not configured for ",
				"inbound messages, and so this facility is not available.",
				"</p>\n");

			return;

		}

		printFormat (
			"<form",
			" action=\"%h\"",
			requestContext.resolveLocalUrl (
				"/route.test.twoWay"),
			" method=\"post\"",
			">\n");

		printFormat (
			"<table class=\"details\">\n");

		printFormat (
			"<tr>\n",

			"<th>Num from</th>\n",

			"<td><input",
			" type=\"text\"",
			" name=\"num_from\"",
			" size=\"32\"",
			" value=\"%h\"",
			requestContext.parameterOrEmptyString (
				"num_from"),
			"></td>\n",

			"</tr>\n");

		printFormat (
			"<tr>\n",

			"<th>Num to</th>\n",

			"<td><input",
			" type=\"text\"",
			" name=\"num_to\"",
			" size=\"32\"",
			" value=\"%h\"",
			requestContext.parameterOrEmptyString (
				"num_to"),
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
