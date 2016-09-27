package wbs.sms.magicnumber.console;

import static wbs.utils.web.HtmlBlockUtils.htmlParagraphClose;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.utils.web.HtmlFormUtils.htmlFormClose;
import static wbs.utils.web.HtmlFormUtils.htmlFormOpenPost;

import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;

@PrototypeComponent ("magicNumberUpdatePart")
public
class MagicNumberUpdatePart
	extends AbstractPagePart {

	@Override
	public
	void renderHtmlBodyContent () {

		// form open

		htmlFormOpenPost ();

		// numbers

		htmlParagraphOpen ();

		formatWriter.writeFormat (
			"Numbers<br>");

		formatWriter.writeFormat (
			"<textarea",
			" name=\"numbers\"",
			" rows=\"16\"",
			" cols=\"40\"",
			">%h</textarea>",
			requestContext.parameterOrEmptyString (
				"numbers"));

		htmlParagraphClose ();

		// form controls

		htmlParagraphOpen ();

		formatWriter.writeFormat (
			"<input",
			" type=\"submit\"",
			" name=\"create\"",
			" value=\"create magic numbers\"",
			">");

		formatWriter.writeFormat (
			"<input",
			" type=\"submit\"",
			" name=\"delete\"",
			" value=\"delete magic numbers\"",
			">");

		htmlParagraphClose ();

		// form close

		htmlFormClose ();

	}

}
