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
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.sms.message.core.console.MessageConsoleHelper;
import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageSearch;
import wbs.sms.message.core.model.MessageSearch.MessageSearchOrder;
import wbs.sms.route.core.console.RouteConsoleHelper;
import wbs.sms.route.core.model.RouteRec;

import wbs.utils.string.FormatWriter;

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
	ConsoleRequestContext requestContext;

	@SingletonDependency
	RouteConsoleHelper routeHelper;

	// state

	RouteRec route;

	Collection <MessageRec> messages;

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
					transaction,
					search);

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
				"received it from the aggregator. It will also show messages ",
				"sent back out by the system, allowing an interaction over ",
				"several messages in and out.");

			htmlParagraphWriteFormatWarning (
				formatWriter,
				"Please note, that this is intended primarily for testing, ",
				"and any other usage should instead be performed using a ",
				"separate facility designed for that specific purpose.");

			if (! route.getCanReceive ()) {

				htmlParagraphWriteFormatError (
					formatWriter,
					"This route is not configured for inbound messages, and ",
					"so this facility is not available.");

				return;

			}

			htmlFormOpenPostAction (
				formatWriter,
				requestContext.resolveLocalUrl (
					"/route.test.twoWay"));

			htmlTableOpenDetails (
				formatWriter);

			htmlTableDetailsRowWriteHtml (
				formatWriter,
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
				formatWriter,
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
				formatWriter,
				"Message",
				stringFormat (
					"<textarea",
					" name=\"message\"",
					" rows=\"3\"",
					" cols=\"64\"",
					"></textarea>"));

			htmlTableClose (
				formatWriter);

			htmlParagraphOpen (
				formatWriter);

			formatWriter.writeLineFormat (
				"<input",
				" type=\"submit\"",
				" value=\"send message\"",
				">");

			htmlParagraphClose (
				formatWriter);

			if (messages != null && messages.size () > 0) {

				htmlHeadingThreeWrite (
					formatWriter,
					"Messages");

				htmlTableOpenList (
					formatWriter);

				htmlTableHeaderRowWrite (
					formatWriter,
					"Number",
					"Message");

				for (
					MessageRec message
						: messages
				) {

					htmlTableRowOpen (
						formatWriter);

					htmlTableCellWrite (
						formatWriter,
						message.getDirection () == MessageDirection.in
							? message.getNumTo ()
							: message.getNumFrom ());

					htmlTableCellWrite (
						formatWriter,
						message.getText ().getText ());

					htmlTableCellClose (
						formatWriter);

				}

				htmlTableClose (
					formatWriter);

			}

			htmlFormClose (
				formatWriter);

		}

	}

}
