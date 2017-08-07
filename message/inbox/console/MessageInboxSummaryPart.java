package wbs.sms.message.inbox.console;

import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringFormatLazy;
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
import wbs.console.priv.UserPrivChecker;

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

import wbs.utils.string.FormatWriter;

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
	UserPrivChecker privChecker;

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
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContent");

		) {

			htmlFormOpenPost (
				formatWriter);

			htmlTableOpenList (
				formatWriter);

			htmlTableHeaderRowWrite (
				formatWriter,
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

				htmlTableRowSeparatorWrite (
					formatWriter);

				// message

				MessageRec message =
					inbox.getMessage ();

				htmlTableRowOpen (
					formatWriter);

				objectManager.writeTdForObjectMiniLink (
					transaction,
					formatWriter,
					privChecker,
					message);

				objectManager.writeTdForObjectMiniLink (
					transaction,
					formatWriter,
					privChecker,
					message.getNumber ());

				htmlTableCellWrite (
					formatWriter,
					message.getNumTo ());

				htmlTableCellWrite (
					formatWriter,
					userConsoleLogic.timestampWithoutTimezoneString (
						transaction,
						message.getCreatedTime ()));

				htmlTableCellWrite (
					formatWriter,
					integerToDecimalString (
						inbox.getNumAttempts ()));

				htmlTableCellWrite (
					formatWriter,
					userConsoleLogic.timestampWithoutTimezoneString (
						transaction,
						inbox.getNextAttempt ()));

				objectManager.writeTdForObjectMiniLink (
					transaction,
					formatWriter,
					privChecker,
					message.getRoute ());

				htmlTableCellWriteHtml (
					formatWriter,
					stringFormatLazy (
						"<input",
						" type=\"submit\"",
						" name=\"ignore_%h\"",
						integerToDecimalString (
							message.getId ()),
						" value=\"cancel\"",
						">"),
					htmlRowSpanAttribute (3l));

				htmlTableRowClose (
					formatWriter);

				// message text

				htmlTableRowOpen (
					formatWriter);

				htmlTableCellWrite (
					formatWriter,
					message.getText ().getText (),
					htmlColumnSpanAttribute (7l));

				htmlTableRowClose (
					formatWriter);

				// status message

				if (
					isNotNull (
						inbox.getStatusMessage ())
				) {

					htmlTableRowOpen (
						formatWriter);

					htmlTableCellWrite (
						formatWriter,
						inbox.getStatusMessage (),
						htmlColumnSpanAttribute (7l));

					htmlTableRowClose (
						formatWriter);

				}

			}

			htmlTableClose (
				formatWriter);

			htmlFormClose (
				formatWriter);

		}

	}

}
