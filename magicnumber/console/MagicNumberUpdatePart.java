package wbs.sms.magicnumber.console;

import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;

@PrototypeComponent ("magicNumberUpdatePart")
public
class MagicNumberUpdatePart
	extends AbstractPagePart {

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<form method=\"post\">\n");

		printFormat (
			"<p>Numbers<br>\n",
			"<textarea",
			" name=\"numbers\"",
			" rows=\"16\"",
			" cols=\"40\"",
			">%h</textarea></p>\n",
			requestContext.parameterOrEmptyString (
				"numbers"));

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" name=\"create\"",
			" value=\"create magic numbers\"",
			"></p>\n");

		printFormat (
			"<p><input",
			" type=\"submit\"",
			" name=\"delete\"",
			" value=\"delete magic numbers\"",
			"></p>\n");

		printFormat (
			"</form>\n");

	}

}
