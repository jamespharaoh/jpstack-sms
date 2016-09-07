package wbs.sms.number.blacklist.console;

import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;

@PrototypeComponent ("blacklistSearchPart")
public
class BlacklistSearchPart
	extends AbstractPagePart {

	@Override
	public
	void prepare () {

	}

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<table class=\"details\">");

		printFormat (
			"<form",
			" method=\"post\"",
			" action=\"%h\"",
			requestContext.resolveLocalUrl (
				"/blacklist.search"),
			">\n");

		printFormat (
			"<tr>\n",
			"<th>Number</th>\n",

			"<td><input",
			" type=\"text\"",
			" name=\"number\"",
			"></td>\n",

			"</tr>\n");

		printFormat (
			"</table>\n");

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" name=\"Search\"",
			" value=\"Search\">");

		printFormat (
			"</form>\n");

	}

}
