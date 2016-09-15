package wbs.sms.message.core.console;

import wbs.sms.message.core.model.MessageRec;
import wbs.utils.string.FormatWriter;

public
interface MessageConsolePlugin {

	String getCode ();

	void writeMessageSummaryText (
			FormatWriter formatWriter,
			MessageRec message);

	void writeMessageSummaryHtml (
			FormatWriter formatWriter,
			MessageRec message);

}
