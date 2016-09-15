package wbs.sms.message.inbox.console;

import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.web.HtmlAttributeUtils.htmlColumnSpanAttribute;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellWriteHtml;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenList;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowOpen;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowSeparatorWrite;
import static wbs.utils.web.HtmlUtils.htmlFormClose;
import static wbs.utils.web.HtmlUtils.htmlFormOpenMethod;

import java.util.List;

import wbs.console.helper.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.model.InboxObjectHelper;
import wbs.sms.message.inbox.model.InboxRec;

@PrototypeComponent ("messageInboxSummaryPart")
public
class MessageInboxSummaryPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	InboxObjectHelper inboxHelper;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// state

	List <InboxRec> inboxes;

	// implementation

	@Override
	public
	void prepare () {

		inboxes =
			inboxHelper.findPendingLimit (
				1000l);

	}

	@Override
	public
	void renderHtmlBodyContent () {

		htmlFormOpenMethod (
			"post");

		htmlTableOpenList ();

		htmlTableHeaderRowWrite (
			"Message",
			"From",
			"To",
			"Created",
			"Tries",
			"Next try",
			"Route",
			"Actions");

		for (
			InboxRec inbox
				: inboxes
		) {

			htmlTableRowSeparatorWrite ();

			// message

			MessageRec message =
				inbox.getMessage ();

			htmlTableRowOpen ();

			objectManager.writeTdForObjectMiniLink (
				message);

			objectManager.writeTdForObjectMiniLink (
				message.getNumber ());

			htmlTableCellWrite (
				message.getNumTo ());

			htmlTableCellWrite (
				userConsoleLogic.timestampWithoutTimezoneString (
					message.getCreatedTime ()));

			htmlTableCellWrite (
				integerToDecimalString (
					inbox.getNumAttempts ()));

			htmlTableCellWrite (
				userConsoleLogic.timestampWithoutTimezoneString (
					inbox.getNextAttempt ()));

			objectManager.writeTdForObjectMiniLink (
				message.getRoute ());

			htmlTableCellWriteHtml (
				stringFormat (
					"<input",
					" type=\"submit\"",
					" name=\"ignore_%h\"",
					message.getId (),
					" value=\"cancel\"",
					">"));

			htmlTableRowClose ();

			// message text

			htmlTableRowOpen ();

			htmlTableCellWrite (
				message.getText ().getText (),
				htmlColumnSpanAttribute (7l));

			htmlTableRowClose ();

			// status message

			if (
				isNotNull (
					inbox.getStatusMessage ())
			) {

				htmlTableRowOpen ();

				htmlTableCellWrite (
					inbox.getStatusMessage (),
					htmlColumnSpanAttribute (7l));

				htmlTableRowClose ();

			}

		}

		htmlTableClose ();

		htmlFormClose ();

	}

}
