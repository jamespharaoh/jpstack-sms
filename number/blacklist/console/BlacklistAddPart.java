package wbs.sms.number.blacklist.console;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.console.part.AbstractPagePart;

@PrototypeComponent ("blacklistAddPart")
public
class BlacklistAddPart
	extends AbstractPagePart {

	@Override
	public
	void prepare () {

	}

	@Override
	public
	void goBodyStuff () {

		printFormat (
			"<table class=\"details\">\n");

		printFormat (
			"<form",
			" method=\"post\"",
			" action=\"%h\"",
			requestContext.resolveLocalUrl (
				"/blacklist.add"),
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
			"<tr>\n",
			"<th>Reason</th>\n",

			"<td><textarea",
			" name=\"reason\"",
			" rows=\"4\"",
			" cols=\"48\"",
			"></textarea></td>");

		printFormat (
			"</table>\n");

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" name=\"Blacklist\"",
			" value=\"Blacklist\"",
			"></p>");

		printFormat (
			"</form>\n");

	}

}
