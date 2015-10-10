package wbs.sms.route.test.console;

import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;

@PrototypeComponent ("routeTestOutPart")
public
class RouteTestOutPart
	extends AbstractPagePart {

	@Override
	public
	void renderHtmlBodyContent () {

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
