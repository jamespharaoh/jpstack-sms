package wbs.sms.message.outbox.console;

import static wbs.utils.etc.EnumUtils.enumEqualSafe;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.web.utils.HtmlAttributeUtils.htmlColumnSpanAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlRowSpanAttribute;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenList;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowSeparatorWrite;

import java.util.Collection;
import java.util.TreeSet;

import lombok.NonNull;

import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.console.priv.UserPrivChecker;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.platform.user.console.UserConsoleLogic;

import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.model.OutboxRec;
import wbs.sms.route.core.console.RouteConsoleHelper;
import wbs.sms.route.core.model.RouteRec;

import wbs.utils.string.FormatWriter;

@PrototypeComponent ("messageOutboxRoutePart")
public
class MessageOutboxRoutePart
	extends AbstractPagePart {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	OutboxConsoleHelper outboxHelper;

	@SingletonDependency
	UserPrivChecker privChecker;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	RouteConsoleHelper routeHelper;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// state

	RouteRec route;

	Collection<OutboxRec> outboxes;

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
				routeHelper.findRequired (
					transaction,
					requestContext.parameterIntegerRequired (
						"routeId"));

			outboxes =
				new TreeSet <> (
					outboxHelper.findLimit (
						transaction,
						route,
						30l));

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

			if (outboxes.size () == 30) {

				formatWriter.writeLineFormat (
					"<p>Only showing first 30 results.</p>");

			}

			htmlTableOpenList (
				formatWriter);

			htmlTableHeaderRowWrite (
				formatWriter,
				"Id",
				"Created",
				"Tries",
				"From",
				"To",
				"Actions");

			if (outboxes.size () == 0) {

				htmlTableRowOpen (
					formatWriter);

				htmlTableCellWrite (
					formatWriter,
					"Nothing to display",
					htmlColumnSpanAttribute (6l));

				htmlTableCellClose (
					formatWriter);

			}

			for (
				OutboxRec outbox
					: outboxes
			) {

				MessageRec message =
					outbox.getMessage ();

				htmlTableRowSeparatorWrite (
					formatWriter);

				htmlTableRowOpen (
					formatWriter);

				htmlTableCellWrite (
					formatWriter,
					integerToDecimalString (
						outbox.getId ()));

				htmlTableCellWrite (
					formatWriter,
					userConsoleLogic.timestampWithTimezoneString (
						transaction,
						message.getCreatedTime ()));

				htmlTableCellWrite (
					formatWriter,
					integerToDecimalString (
						outbox.getTries ()));

				if (
					enumEqualSafe (
						message.getDirection (),
						MessageDirection.in)
				) {

					objectManager.writeTdForObjectMiniLink (
						transaction,
						formatWriter,
						privChecker,
						message.getNumber ());

				} else {

					htmlTableCellWrite (
						formatWriter,
						message.getNumFrom ());

				}

				if (
					enumEqualSafe (
						message.getDirection (),
						MessageDirection.out)
				) {

					objectManager.writeTdForObjectMiniLink (
						transaction,
						formatWriter,
						privChecker,
						message.getNumber ());

				} else {

					htmlTableCellWrite (
						formatWriter,
						message.getNumTo ());

				}

				long rowSpan =
					outbox.getError () != null
						? 3
						: 2;

				htmlTableCellOpen (
					formatWriter,
					htmlRowSpanAttribute (
						rowSpan));

				htmlFormOpenPostAction (
					formatWriter,
					requestContext.resolveLocalUrlFormat (
						"/outbox.route",
						"?routeId=%u",
						integerToDecimalString (
							route.getId ())));

				formatWriter.writeLineFormat (
					"<input",
					" type=\"hidden\"",
					" name=\"messageId\"",
					" value=\"%h\"",
					integerToDecimalString (
						outbox.getId ()),
					">");

				formatWriter.writeLineFormat (
					"<input",
					" type=\"submit\"",
					" name=\"cancel\"",
					" value=\"cancel\"",
					">");

				formatWriter.writeLineFormat (
					"<input",
					" type=\"submit\"",
					" name=\"retry\"",
					" value=\"retry\"",
					">");

				htmlFormClose (
					formatWriter);

				htmlTableCellClose (
					formatWriter);

				htmlTableRowClose (
					formatWriter);

				// row 3 - message text

				htmlTableRowOpen (
					formatWriter);

				htmlTableCellWrite (
					formatWriter,
					message.getText ().getText (),
					htmlColumnSpanAttribute (5l));

				htmlTableRowClose (
					formatWriter);

				// row 4 - error

				if (
					isNotNull (
						outbox.getError ())
				) {

					htmlTableRowOpen (
						formatWriter);

					htmlTableCellWrite (
						formatWriter,
						outbox.getError (),
						htmlColumnSpanAttribute (5l));

					htmlTableRowClose (
						formatWriter);

				}

			}

			htmlTableClose (
				formatWriter);

		}

	}

}
