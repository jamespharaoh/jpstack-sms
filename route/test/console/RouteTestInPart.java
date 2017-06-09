package wbs.sms.route.test.console;

import static wbs.utils.string.StringUtils.stringFormatLazy;
import static wbs.web.utils.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWrite;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWriteFormat;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWriteHtml;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;

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

@PrototypeComponent ("routeTestInPart")
public
class RouteTestInPart
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
				"This facility can be used to insert an inbound message into ",
				"the system, which will then be treated exactly as if we had ",
				"received it from the aggregator.");

			htmlParagraphWrite (
				formatWriter,
				stringFormatLazy (
					"Please note, that this is intended primarily for ",
					"testing, and any other usage should instead be performed ",
					"using a separate facility designed for that specific ",
					"purpose"),
				htmlClassAttribute (
					"warning"));

			if (! route.getCanReceive ()) {

				htmlParagraphWrite (
					formatWriter,
					stringFormatLazy (
						"This route is not configured for inbound messages, ",
						"and so this facility is not available."),
					htmlClassAttribute (
						"error"));

				return;

			}

			htmlFormOpenPostAction (
				formatWriter,
				requestContext.resolveLocalUrl (
					"/route.test.in"));

			htmlTableOpenDetails (
				formatWriter);

			htmlTableDetailsRowWriteHtml (
				formatWriter,
				"Num from",
				() -> formatWriter.writeLineFormat (
					"<input",
					" type=\"text\"",
					" name=\"num_from\"",
					" size=\"32\"",
					">"));

			htmlTableDetailsRowWriteHtml (
				formatWriter,
				"Num to",
				() -> formatWriter.writeLineFormat (
					"<input",
					" type=\"text\"",
					" name=\"num_to\"",
					" size=\"32\"",
					">"));

			htmlTableDetailsRowWriteHtml (
				formatWriter,
				"Message",
				() -> formatWriter.writeLineFormat (
					"<textarea",
					" rows=\"8\"",
					" cols=\"32\"",
					" name=\"message\"",
					"></textarea>"));

			htmlTableClose (
				formatWriter);

			htmlParagraphOpen (
				formatWriter);

			formatWriter.writeLineFormat (
				"<input",
				" type=\"submit\"",
				" value=\"insert message\"",
				">");

			htmlParagraphClose (
				formatWriter);

			htmlFormClose (
				formatWriter);

		}

	}

}
