package wbs.sms.number.blacklist.console;

import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableDetailsRowWriteHtml;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenDetails;
import static wbs.utils.web.HtmlUtils.htmlFormClose;
import static wbs.utils.web.HtmlUtils.htmlFormOpenMethodAction;
import static wbs.utils.web.HtmlUtils.htmlParagraphClose;
import static wbs.utils.web.HtmlUtils.htmlParagraphOpen;

import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;

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
	void renderHtmlBodyContent () {

		htmlTableOpenDetails ();

		htmlFormOpenMethodAction (
			"post",
			requestContext.resolveLocalUrl (
				"/blacklist.add"));

		htmlTableDetailsRowWriteHtml (
			"Number",
			stringFormat (
				"<input",
				" type=\"text\"",
				" name=\"number\"",
				">"));

		htmlTableDetailsRowWriteHtml (
			"Reason",
			stringFormat (
				"<textarea",
				" name=\"reason\"",
				" rows=\"4\"",
				" cols=\"48\"",
				"></textarea>"));

		htmlTableClose ();

		htmlParagraphOpen ();

		formatWriter.writeLineFormat (
			"<input",
			" type=\"submit\"",
			" name=\"Blacklist\"",
			" value=\"Blacklist\"",
			">");

		htmlParagraphClose ();

		htmlFormClose ();

		htmlTableClose ();

	}

}
