package wbs.sms.message.core.console;

import static wbs.framework.utils.etc.Misc.dateToInstant;

import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import wbs.console.misc.TimeFormatter;
import wbs.console.part.AbstractPagePart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.web.PageNotFoundException;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.report.model.MessageReportRec;

@PrototypeComponent ("messageReportsPart")
public
class MessageReportsPart
	extends AbstractPagePart {

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	TimeFormatter timeFormatter;

	MessageRec message;
	Set<MessageReportRec> messageReports;

	@Override
	public
	void prepare () {

		message =
			messageHelper.find (
				requestContext.stuffInt ("messageId"));

		if (message == null)
			throw new PageNotFoundException ();

		messageReports =
			new TreeSet<MessageReportRec> (
				message.getReports ());

	}

	@Override
	public
	void renderHtmlBodyContent () {

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th colspan=\"2\">Time</th>\n",
			"<th>Status</th>\n",
			"<th>Code</th>\n",
			"</tr>\n");

		if (messageReports.isEmpty ()) {

			printFormat (
				"<tr>\n",
				"<td colspan=\"4\">No reports</td>\n",
				"</tr>\n");

		} else {

			for (MessageReportRec messageReport
					: messageReports) {

				long interval =
					+ messageReport.getReceivedTime ().getTime ()
					- message.getProcessedTime ().getTime ();

				printFormat (
					"<tr>\n",

					"<td>%h</td>\n",
					timeFormatter.instantToTimestampString (
						timeFormatter.defaultTimezone (),
						dateToInstant (
							messageReport.getReceivedTime ())),

					"<td>%h</td>\n",
					requestContext.prettyMsInterval (interval),

					"%s\n",
					MessageConsoleStuff.tdForMessageStatus (
						messageReport.getNewMessageStatus ()));

				if (messageReport.getCode () != null) {

					printFormat (
						"<td>%h</td>\n",
						messageReport.getCode ());

				} else if (messageReport.getMessageReportCode () != null) {

					printFormat (
						"<td>%h</td>\n",
						messageReport.getMessageReportCode ().toString ());

				} else {

					printFormat (
						"<td>-</td>\n");

				}

				printFormat (
					"</tr>\n");

			}

		}

		printFormat (
			"</table>\n");

	}

}
