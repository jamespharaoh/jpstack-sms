package wbs.sms.number.blacklist.console;

import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWriteHtml;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;

import lombok.NonNull;

import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

@PrototypeComponent ("blacklistSearchPart")
public
class BlacklistSearchPart
	extends AbstractPagePart {

	// singleton dependency

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContent");

		) {

			htmlFormOpenPostAction (
				requestContext.resolveLocalUrl (
					"/blacklist.search"));

			htmlTableOpenDetails ();

			htmlTableDetailsRowWriteHtml (
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

	}

}
