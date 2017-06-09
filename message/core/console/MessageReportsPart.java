package wbs.sms.message.core.console;

import static wbs.utils.etc.LogicUtils.ifNotNullThenElseEmDash;
import static wbs.web.utils.HtmlAttributeUtils.htmlColumnSpanAttribute;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenList;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;

import java.util.Set;
import java.util.TreeSet;

import lombok.NonNull;

import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.platform.user.console.UserConsoleLogic;

import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.report.model.MessageReportRec;

import wbs.utils.string.FormatWriter;

@PrototypeComponent ("messageReportsPart")
public
class MessageReportsPart
	extends AbstractPagePart {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MessageConsoleLogic messageConsoleLogic;

	@SingletonDependency
	MessageConsoleHelper messageHelper;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// state

	MessageRec message;
	Set <MessageReportRec> messageReports;

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

			message =
				messageHelper.findFromContextRequired (
					transaction);

			messageReports =
				new TreeSet<> (
					message.getReports ());

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

			htmlTableOpenList (
				formatWriter);

			htmlTableHeaderRowWrite (
				formatWriter,
				"Time",
				null,
				"Status",
				"Their code",
				"Their description");

			if (messageReports.isEmpty ()) {

				htmlTableRowOpen (
					formatWriter);

				htmlTableCellWrite (
					formatWriter,
					"No reports",
					htmlColumnSpanAttribute (5l));

				htmlTableRowClose (
					formatWriter);

			} else {

				for (
					MessageReportRec messageReport
						: messageReports
				) {

					htmlTableRowOpen (
						formatWriter);

					htmlTableCellWrite (
						formatWriter,
						userConsoleLogic.timestampWithTimezoneString (
							transaction,
							messageReport.getReceivedTime ()));

					htmlTableCellWrite (
						formatWriter,
						userConsoleLogic.prettyDuration (
							transaction,
							message.getProcessedTime (),
							messageReport.getReceivedTime ()));

					messageConsoleLogic.writeTdForMessageStatus (
						transaction,
						formatWriter,
						messageReport.getNewMessageStatus ());

					htmlTableCellWrite (
						formatWriter,
						ifNotNullThenElseEmDash (
							messageReport.getTheirCode (),
							() -> messageReport.getTheirCode ().getText ()));

					htmlTableCellWrite (
						formatWriter,
						ifNotNullThenElseEmDash (
							messageReport.getTheirDescription (),
							() -> messageReport.getTheirDescription ().getText ()));

					htmlTableRowClose (
						formatWriter);

				}

			}

			htmlTableClose (
				formatWriter);

		}

	}

}
