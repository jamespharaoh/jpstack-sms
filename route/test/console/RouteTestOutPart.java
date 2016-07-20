package wbs.sms.route.test.console;

import javax.inject.Inject;

import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.sms.route.core.console.RouteConsoleHelper;
import wbs.sms.route.core.model.RouteRec;

@PrototypeComponent ("routeTestOutPart")
public
class RouteTestOutPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	RouteConsoleHelper routeHelper;

	// state

	RouteRec route;

	// implementation

	@Override
	public
	void prepare () {

		route =
			routeHelper.findRequired (
				requestContext.stuffInt (
					"routeId"));

	}

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<p>This facility can be used to insert an outbound message into ",
			"the system, which will then be sent out to the aggregator as ",
			"normal.</p>\n");

		printFormat (
			"<p class=\"warning\">Please note, that this is intended ",
			"primarily for testing, and any other usage should instead be ",
			"performed using a separate facility designed for that specific ",
			"purpose.\n");

		if (! route.getCanSend ()) {

			printFormat (
				"<p class=\"error\">This route is not configured for outbound ",
				"messages, and so this facility is not available.</p>\n");

			return;

		}

		printFormat (
			"<form",
			" action=\"%h\"",
			requestContext.resolveLocalUrl (
				"/route.test.out"),
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
			"></td>\n",
			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Num to</th>\n",
			"<td>",
			"<input",
			" type=\"text\"",
			" name=\"num_to\"",
			" size=\"32\"",
			">",
			"</td>\n",
			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Message</th>\n",
			"<td>",
			"<textarea",
			" rows=\"8\"",
			" cols=\"32\"",
			" name=\"message\"",
			">",
			"</textarea>",
			"</td>\n",
			"</tr>\n");

		printFormat (
			"</table>\n");

		printFormat (
			"<p>",
			"<input",
			" type=\"submit\"",
			" value=\"insert message\"",
			">",
			"</p>\n");

		printFormat (
			"</form>\n");

	}

}
