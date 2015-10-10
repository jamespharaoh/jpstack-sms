package wbs.sms.message.core.console;

import wbs.console.part.PagePart;
import wbs.sms.message.core.model.MessageRec;

public
interface MessageConsolePlugin {

	String getCode ();

	String messageSummary (
			MessageRec message);

	PagePart makeMessageSummaryPart (
			MessageRec message);

}
