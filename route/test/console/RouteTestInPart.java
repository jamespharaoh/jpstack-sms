package wbs.sms.route.test.console;

import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.web.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphClose;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphWrite;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphWriteFormat;
import static wbs.utils.web.HtmlFormUtils.htmlFormClose;
import static wbs.utils.web.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableDetailsRowWriteHtml;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenDetails;

import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.sms.route.core.console.RouteConsoleHelper;
import wbs.sms.route.core.model.RouteRec;

@PrototypeComponent ("routeTestInPart")
public
class RouteTestInPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	RouteConsoleHelper routeHelper;

	// state

	RouteRec route;

	// implementation

	@Override
	public
	void prepare () {

		route =
			routeHelper.findRequired (
				requestContext.stuffInteger (
					"routeId"));

	}

	@Override
	public
	void renderHtmlBodyContent () {

		htmlParagraphWriteFormat (
			"This facility can be used to insert an inbound message into the ",
			"system, which will then be treated exactly as if we had received ",
			"it from the aggregator.");

		htmlParagraphWrite (
			stringFormat (
				"Please note, that this is intended primarily for testing, ",
				"and any other usage should instead be performed using a ",
				"separate facility designed for that specific purpose"),
			htmlClassAttribute (
				"warning"));

		if (! route.getCanReceive ()) {

			htmlParagraphWrite (
				stringFormat (
					"This route is not configured for inbound messages, and so",
					"this facility is not available."),
				htmlClassAttribute (
					"error"));

			return;

		}

		htmlFormOpenPostAction (
			requestContext.resolveLocalUrl (
				"/route.test.in"));

		htmlTableOpenDetails ();

		htmlTableDetailsRowWriteHtml (
			"Num from",
			() -> formatWriter.writeLineFormat (
				"<input",
				" type=\"text\"",
				" name=\"num_from\"",
				" size=\"32\"",
				">"));

		htmlTableDetailsRowWriteHtml (
			"Num to",
			() -> formatWriter.writeLineFormat (
				"<input",
				" type=\"text\"",
				" name=\"num_to\"",
				" size=\"32\"",
				">"));

		htmlTableDetailsRowWriteHtml (
			"Message",
			() -> formatWriter.writeLineFormat (
				"<textarea",
				" rows=\"8\"",
				" cols=\"32\"",
				" name=\"message\"",
				"></textarea>"));

		htmlTableClose ();

		htmlParagraphOpen ();

		formatWriter.writeLineFormat (
			"<input",
			" type=\"submit\"",
			" value=\"insert message\"",
			">");

		htmlParagraphClose ();

		htmlFormClose ();

	}

}
