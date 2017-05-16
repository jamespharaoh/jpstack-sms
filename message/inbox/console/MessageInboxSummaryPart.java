package wbs.sms.message.inbox.console;

import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.web.utils.HtmlAttributeUtils.htmlColumnSpanAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlRowSpanAttribute;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenPost;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWriteHtml;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenList;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowSeparatorWrite;

import java.util.List;

import lombok.NonNull;

import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

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

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// state

	List <InboxRec> inboxes;

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

			inboxes =
				inboxHelper.findPendingLimit (
					transaction,
					1000l);

		}

	}

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

			htmlFormOpenPost ();

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
					transaction,
					message);

				objectManager.writeTdForObjectMiniLink (
					transaction,
					message.getNumber ());

				htmlTableCellWrite (
					message.getNumTo ());

				htmlTableCellWrite (
					userConsoleLogic.timestampWithoutTimezoneString (
						transaction,
						message.getCreatedTime ()));

				htmlTableCellWrite (
					integerToDecimalString (
						inbox.getNumAttempts ()));

				htmlTableCellWrite (
					userConsoleLogic.timestampWithoutTimezoneString (
						transaction,
						inbox.getNextAttempt ()));

				objectManager.writeTdForObjectMiniLink (
					transaction,
					message.getRoute ());

				htmlTableCellWriteHtml (
					stringFormat (
						"<input",
						" type=\"submit\"",
						" name=\"ignore_%h\"",
						integerToDecimalString (
							message.getId ()),
						" value=\"cancel\"",
						">"),
					htmlRowSpanAttribute (3l));

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

}
