package wbs.sms.route.test.console;

import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.web.utils.HtmlBlockUtils.htmlHeadingThreeWrite;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWriteFormat;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWriteFormatError;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWriteFormatWarning;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWriteHtml;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenList;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;

import java.util.Collection;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.sms.message.core.console.MessageConsoleHelper;
import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageSearch;
import wbs.sms.message.core.model.MessageSearch.MessageSearchOrder;
import wbs.sms.route.core.console.RouteConsoleHelper;
import wbs.sms.route.core.model.RouteRec;

@PrototypeComponent ("routeTestTwoWayPart")
public
class RouteTestTwoWayPart
	extends AbstractPagePart {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MessageConsoleHelper messageHelper;

	@SingletonDependency
	RouteConsoleHelper routeHelper;

	// state

	RouteRec route;

	Collection <MessageRec> messages;

	// implementation

	@Override
	public
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"prepare");

		route =
			routeHelper.findFromContextRequired ();

		Optional <String> numberOptional =
			requestContext.parameter (
				"num_from");

		if (
			optionalIsNotPresent (
				numberOptional)
		) {
			return;
		}

		MessageSearch search =
			new MessageSearch ()

			.number (
				numberOptional.get ())

			.maxResults (
				20l)

			.orderBy (
				MessageSearchOrder.createdTimeDesc);

		messages =
			messageHelper.search (
				taskLogger,
				search);

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		htmlParagraphWriteFormat (
			"This facility can be used to insert an inbound message into the ",
			"system, which will then be treated exactly as if we had received ",
			"it from the aggregator. It will also show messages sent back out",
			"by the system, allowing an interaction over several messages in ",
			"and out.");

		htmlParagraphWriteFormatWarning (
			"Please note, that this is intended primarily for testing, and ",
			"any other usage should instead be performed using a separate ",
			"facility designed for that specific purpose.");

		if (! route.getCanReceive ()) {

			htmlParagraphWriteFormatError (
				"This route is not configured for inbound messages, and so ",
				"this facility is not available.");

			return;

		}

		htmlFormOpenPostAction (
			requestContext.resolveLocalUrl (
				"/route.test.twoWay"));

		htmlTableOpenDetails ();

		htmlTableDetailsRowWriteHtml (
			"Num from",
			stringFormat (
				"<input",
				" type=\"text\"",
				" name=\"num_from\"",
				" size=\"32\"",
				" value=\"%h\"",
				requestContext.parameterOrEmptyString (
					"num_from"),
				">"));

		htmlTableDetailsRowWriteHtml (
			"Num to",
			stringFormat (
				"<input",
				" type=\"text\"",
				" name=\"num_to\"",
				" size=\"32\"",
				" value=\"%h\"",
				requestContext.parameterOrEmptyString (
					"num_to"),
				">"));

		htmlTableDetailsRowWriteHtml (
			"Message",
			stringFormat (
				"<textarea",
				" name=\"message\"",
				" rows=\"3\"",
				" cols=\"64\"",
				"></textarea>"));

		htmlTableClose ();

		htmlParagraphOpen ();

		formatWriter.writeLineFormat (
			"<input",
			" type=\"submit\"",
			" value=\"send message\"",
			">");

		htmlParagraphClose ();

		if (messages != null && messages.size () > 0) {

			htmlHeadingThreeWrite (
				"Messages");

			htmlTableOpenList ();

			htmlTableHeaderRowWrite (
				"Number",
				"Message");

			for (
				MessageRec message
					: messages
			) {

				htmlTableRowOpen ();

				htmlTableCellWrite (
					message.getDirection () == MessageDirection.in
						? message.getNumTo ()
						: message.getNumFrom ());

				htmlTableCellWrite (
					message.getText ().getText ());

				htmlTableCellClose ();

			}

			htmlTableClose ();

		}

		htmlFormClose ();

	}

}
