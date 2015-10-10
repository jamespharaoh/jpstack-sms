package wbs.sms.message.core.console;

import wbs.console.helper.EnumConsoleHelper;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.sms.message.core.model.MessageStatus;

@SingletonComponent ("messageStatusConsoleHelper")
public
class MessageStatusConsoleHelper
	extends EnumConsoleHelper<MessageStatus> {

	{

		enumClass (MessageStatus.class);

		auto ();

	}

}
