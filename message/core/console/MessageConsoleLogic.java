package wbs.sms.message.core.console;

import wbs.sms.message.core.model.MessageRec;

public
interface MessageConsoleLogic {

	String messageContentText (
			MessageRec message);

	String messageContentHtml (
			MessageRec message);

}
