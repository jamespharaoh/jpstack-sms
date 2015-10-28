package wbs.sms.message.core.console;

import wbs.console.helper.EnumConsoleHelper;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.sms.message.core.model.MessageDirection;

@SingletonComponent ("messageDirectionConsoleHelper")
public
class MessageDirectionConsoleHelper
	extends EnumConsoleHelper<MessageDirection> {

	{

		enumClass (
			MessageDirection.class);

		auto ();

	}

}
