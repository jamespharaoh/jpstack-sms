package wbs.sms.message.outbox.console;

import static wbs.utils.etc.EnumUtils.enumEqualSafe;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.web.HtmlAttributeUtils.htmlColumnSpanAttribute;
import static wbs.utils.web.HtmlAttributeUtils.htmlRowSpanAttribute;
import static wbs.utils.web.HtmlFormUtils.htmlFormClose;
import static wbs.utils.web.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellOpen;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenList;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowOpen;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowSeparatorWrite;

import java.util.Collection;
import java.util.TreeSet;

import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.model.OutboxRec;
import wbs.sms.route.core.console.RouteConsoleHelper;
import wbs.sms.route.core.model.RouteRec;

@PrototypeComponent ("messageOutboxRoutePart")
public
class MessageOutboxRoutePart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	OutboxConsoleHelper outboxHelper;

	@SingletonDependency
	RouteConsoleHelper routeHelper;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// state

	RouteRec route;

	Collection<OutboxRec> outboxes;

	@Override
	public
	void prepare () {

		route =
			routeHelper.findRequired (
				requestContext.parameterIntegerRequired (
					"routeId"));

		outboxes =
			new TreeSet <> (
				outboxHelper.findLimit (
					route,
					30l));

	}

	@Override
	public
	void renderHtmlBodyContent () {

		if (outboxes.size () == 30) {

			formatWriter.writeLineFormat (
				"<p>Only showing first 30 results.</p>");

		}

		htmlTableOpenList ();

		htmlTableHeaderRowWrite (
			"Id",
			"Created",
			"Tries",
			"From",
			"To",
			"Actions");

		if (outboxes.size () == 0) {

			htmlTableRowOpen ();

			htmlTableCellWrite (
				"Nothing to display",
				htmlColumnSpanAttribute (6l));

			htmlTableCellClose ();

		}

		for (
			OutboxRec outbox
				: outboxes
		) {

			MessageRec message =
				outbox.getMessage ();

			htmlTableRowSeparatorWrite ();

			htmlTableRowOpen ();

			htmlTableCellWrite (
				integerToDecimalString (
					outbox.getId ()));

			htmlTableCellWrite (
				userConsoleLogic.timestampWithTimezoneString (
					message.getCreatedTime ()));

			htmlTableCellWrite (
				integerToDecimalString (
					outbox.getTries ()));

			if (
				enumEqualSafe (
					message.getDirection (),
					MessageDirection.in)
			) {

				objectManager.writeTdForObjectMiniLink (
					message.getNumber ());

			} else {

				htmlTableCellWrite (
					message.getNumFrom ());

			}

			if (
				enumEqualSafe (
					message.getDirection (),
					MessageDirection.out)
			) {

				objectManager.writeTdForObjectMiniLink (
					message.getNumber ());

			} else {

				htmlTableCellWrite (
					message.getNumTo ());

			}

			long rowSpan =
				outbox.getError () != null
					? 3
					: 2;

			htmlTableCellOpen (
				htmlRowSpanAttribute (
					rowSpan));

			htmlFormOpenPostAction (
				requestContext.resolveLocalUrl (
					stringFormat (
						"/outbox.route",
						"?routeId=%u",
						route.getId ())));

			formatWriter.writeLineFormat (
				"<input",
				" type=\"hidden\"",
				" name=\"messageId\"",
				" value=\"%h\"",
				outbox.getId (),
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

			htmlFormClose ();

			htmlTableCellClose ();

			htmlTableRowClose ();

			// row 3 - message text

			htmlTableRowOpen ();

			htmlTableCellWrite (
				message.getText ().getText (),
				htmlColumnSpanAttribute (5l));

			htmlTableRowClose ();

			// row 4 - error

			if (
				isNotNull (
					outbox.getError ())
			) {

				htmlTableRowOpen ();

				htmlTableCellWrite (
					outbox.getError (),
					htmlColumnSpanAttribute (5l));

				htmlTableRowClose ();

			}

		}

		htmlTableClose ();

	}

}
