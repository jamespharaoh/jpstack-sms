package wbs.sms.route.test.console;

import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.sms.route.core.console.RouteConsoleHelper;
import wbs.sms.route.core.model.RouteRec;

@PrototypeComponent ("routeTestInPart")
public
class RouteTestInPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	RouteConsoleHelper routeHelper;

	// state

	RouteRec route;

	// implementation

	@Override
	public
	void prepare () {

		route =
			routeHelper.findRequired (
				requestContext.stuffInteger (
					"routeId"));

	}

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<p>This facility can be used to insert an inbound message into ",
			"the system, which will then be treated exactly as if we had ",
			"received it from the aggregator.</p>\n");

		printFormat (
			"<p class=\"warning\">Please note, that this is intended ",
			"primarily for testing, and any other usage should instead be ",
			"performed using a separate facility designed for that specific ",
			"purpose.\n");

		if (! route.getCanReceive ()) {

			printFormat (
				"<p class=\"error\">This route is not configured for inbound ",
				"messages, and so this facility is not available.</p>\n");

			return;

		}

		printFormat (
			"<form",
			" action=\"%h\"",
			requestContext.resolveLocalUrl (
				"/route.test.in"),
			" method=\"post\"",
			">\n");

		printFormat (
			"<table class=\"details\">\n");

		printFormat (
			"<tr>\n",
			"<th>Num from</th>\n",
			"<td>",
			"<input",
			" type=\"text\"",
			" name=\"num_from\"",
			" size=\"32\"",
			">",
			"</td>\n",
			"</tr>\n");

		printFormat (
			"<tr>\n",
			"<th>Num to</th>\n",
			"<td>",
			"<input",
			" type=\"text\"",
			" name=\"num_to\"",
			" size=\"32\"></td>\n",
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
