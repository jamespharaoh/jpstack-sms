package wbs.sms.number.blacklist.console;

import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;

import lombok.NonNull;

import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("blacklistSearchPart")
public
class BlacklistSearchPart
	extends AbstractPagePart {

	@Override
	public
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		htmlFormOpenPostAction (
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

	private void htmlTableDetailsRowHtml (
			String string,
			String stringFormat) {

		// TODO Auto-generated method stub

	}

}
