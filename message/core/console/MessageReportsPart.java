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

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;

import wbs.platform.user.console.UserConsoleLogic;

import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.report.model.MessageReportRec;

@PrototypeComponent ("messageReportsPart")
public
class MessageReportsPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	MessageConsoleLogic messageConsoleLogic;

	@SingletonDependency
	MessageObjectHelper messageHelper;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// state

	MessageRec message;
	Set <MessageReportRec> messageReports;

	// implementation

	@Override
	public
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		message =
			messageHelper.findRequired (
				requestContext.stuffInteger (
					"messageId"));

		messageReports =
			new TreeSet<MessageReportRec> (
				message.getReports ());

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		htmlTableOpenList ();

		htmlTableHeaderRowWrite (
			"Time",
			null,
			"Status",
			"Their code",
			"Their description");

		if (messageReports.isEmpty ()) {

			htmlTableRowOpen ();

			htmlTableCellWrite (
				"No reports",
				htmlColumnSpanAttribute (5l));

			htmlTableRowClose ();

		} else {

			for (
				MessageReportRec messageReport
					: messageReports
			) {

				htmlTableRowOpen ();

				htmlTableCellWrite (
					userConsoleLogic.timestampWithTimezoneString (
						messageReport.getReceivedTime ()));

				htmlTableCellWrite (
					userConsoleLogic.prettyDuration (
						message.getProcessedTime (),
						messageReport.getReceivedTime ()));

				messageConsoleLogic.writeTdForMessageStatus (
					formatWriter,
					messageReport.getNewMessageStatus ());

				htmlTableCellWrite (
					ifNotNullThenElseEmDash (
						messageReport.getTheirCode (),
						() -> messageReport.getTheirCode ().getText ()));

				htmlTableCellWrite (
					ifNotNullThenElseEmDash (
						messageReport.getTheirDescription (),
						() -> messageReport.getTheirDescription ().getText ()));

				htmlTableRowClose ();

			}

		}

		htmlTableClose ();

	}

}
