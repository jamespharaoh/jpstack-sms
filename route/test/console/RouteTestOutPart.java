package wbs.sms.route.test.console;

import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringFormatLazy;
import static wbs.web.utils.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWrite;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWriteFormat;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWriteHtml;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;

import lombok.NonNull;

import wbs.console.part.AbstractPagePart;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.sms.route.core.console.RouteConsoleHelper;
import wbs.sms.route.core.model.RouteRec;

import wbs.utils.string.FormatWriter;

@PrototypeComponent ("routeTestOutPart")
public
class RouteTestOutPart
	extends AbstractPagePart {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	RouteConsoleHelper routeHelper;

	// state

	RouteRec route;

	// implementation

	@Override
	public
	void prepare (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepare");

		) {

			route =
				routeHelper.findFromContextRequired (
					transaction);

		}

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContent");

		) {

			htmlParagraphWriteFormat (
				formatWriter,
				"This facility can be used to insert an outbound message into ",
				"the system, which will then be sent out to the aggregator as ",
				"normal");

			htmlParagraphWrite (
				formatWriter,
				stringFormatLazy (
					"Please note, that this is intended primarily for ",
					"testing, and any other usage should instead be performed ",
					"using a separate facility designed for that specific ",
					"purpose."),
				htmlClassAttribute (
					"warning"));

			if (! route.getCanSend ()) {

				htmlParagraphWrite (
					formatWriter,
					stringFormatLazy (
						"This route is not configured for outbound messages, ",
						"and so this facility is not available."),
					htmlClassAttribute (
						"error"));

				return;

			}

			htmlFormOpenPostAction (
				formatWriter,
				requestContext.resolveLocalUrl (
					"/route.test.out"));

			htmlTableOpenDetails (
				formatWriter);

			htmlTableDetailsRowWriteHtml (
				formatWriter,
				"Num free",
				stringFormat (
					"<input",
					" type=\"text\"",
					" name=\"num_from\"",
					" size=\"32\"",
					">"));

			htmlTableDetailsRowWriteHtml (
				formatWriter,
				"Num to",
				stringFormat (
					"<input",
					" type=\"text\"",
					" name=\"num_to\"",
					" size=\"32\"",
					">"));

			htmlTableDetailsRowWriteHtml (
				formatWriter,
				"Message",
				stringFormat (
					"<textarea",
					" rows=\"8\"",
					" cols=\"32\"",
					" name=\"message\"",
					"></textarea>"));

			htmlTableRowClose (
				formatWriter);

			htmlTableClose (
				formatWriter);

			htmlParagraphOpen (
				formatWriter);

			formatWriter.writeLineFormat (
				"<input",
				" type=\"submit\"",
				" value=\"insert message\"",
				">");

			htmlParagraphOpen (
				formatWriter);

			htmlTableClose (
				formatWriter);

		}

	}

}
