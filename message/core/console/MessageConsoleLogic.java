package wbs.sms.message.core.console;

import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;

public
interface MessageConsoleLogic {

	String messageContentText (
			MessageRec message);

	String messageContentHtml (
			MessageRec message);

	//@Deprecated
	String tdForMessageStatus (
			MessageStatus messageStatus);

	//@Deprecated
	String classForMessage (
			MessageRec message);

	//@Deprecated
	String classForMessageStatus (
			MessageStatus messageStatus);

	//@Deprecated
	String classForMessageDirection (
			MessageDirection direction);

	//@Deprecated
	char charForMessageStatus (
			MessageStatus messageStatus);

}
