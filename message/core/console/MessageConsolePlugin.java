package wbs.sms.message.core.console;

import wbs.framework.database.Transaction;

import wbs.sms.message.core.model.MessageRec;

import wbs.utils.string.FormatWriter;

public
interface MessageConsolePlugin {

	String getCode ();

	void writeMessageSummaryText (
			Transaction parentTransaction,
			FormatWriter formatWriter,
			MessageRec message);

	void writeMessageSummaryHtml (
			Transaction parentTransaction,
			FormatWriter formatWriter,
			MessageRec message);

}
