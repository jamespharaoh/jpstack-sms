package wbs.sms.message.core.console;

import wbs.framework.database.Transaction;

import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;

import wbs.utils.string.FormatWriter;

public
interface MessageConsoleLogic {

	void writeMessageContentText (
			Transaction parentTransaction,
			FormatWriter formatWriter,
			MessageRec message);

	void writeMessageContentHtml (
			Transaction parentTransaction,
			FormatWriter formatWriter,
			MessageRec message);

	void writeTdForMessageStatus (
			Transaction parentTransaction,
			FormatWriter formatWriter,
			MessageStatus messageStatus);

	String classForMessage (
			MessageRec message);

	String classForMessageStatus (

			MessageStatus messageStatus);

	String classForMessageDirection (
			MessageDirection direction);

	char charForMessageStatus (
			MessageStatus messageStatus);

}
