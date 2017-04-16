package wbs.sms.message.core.console;

import wbs.framework.logging.TaskLogger;

import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;

import wbs.utils.string.FormatWriter;

public
interface MessageConsoleLogic {

	void writeMessageContentText (
			FormatWriter formatWriter,
			MessageRec message);

	void writeMessageContentHtml (
			TaskLogger parentTaskLogger,
			FormatWriter formatWriter,
			MessageRec message);

	void writeTdForMessageStatus (
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
