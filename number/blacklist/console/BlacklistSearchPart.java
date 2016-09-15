package wbs.sms.number.blacklist.console;

import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenDetails;
import static wbs.utils.web.HtmlUtils.htmlFormClose;
import static wbs.utils.web.HtmlUtils.htmlFormOpenMethodAction;
import static wbs.utils.web.HtmlUtils.htmlParagraphClose;
import static wbs.utils.web.HtmlUtils.htmlParagraphOpen;

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

		htmlFormOpenMethodAction (
			"post",
			requestContext.resolveLocalUrl (
				"/blacklist.search"));

		htmlTableOpenDetails ();

		htmlTableDetailsRowHtml (
			"Number",
			stringFormat (
				"<input",
				" type=\"text\"",
				" name=\"number\"",
				">"));

		htmlTableClose ();

		htmlParagraphOpen ();

		formatWriter.writeLineFormat (
			"<input",
			" type=\"submit\"",
			" name=\"Search\"",
			" value=\"Search\">");

		htmlParagraphClose ();

		htmlFormClose ();

	}

	private
	void htmlTableDetailsRowHtml (
			String string,
			String stringFormat) {

		// TODO Auto-generated method stub
		
	}

}
