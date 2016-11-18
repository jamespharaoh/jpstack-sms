package wbs.sms.number.blacklist.console;

import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenMethodAction;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWriteHtml;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;

import lombok.NonNull;

import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.logging.TaskLogger;

@PrototypeComponent ("blacklistAddPart")
public
class BlacklistAddPart
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
