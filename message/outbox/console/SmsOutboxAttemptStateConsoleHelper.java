package wbs.sms.message.outbox.console;

import wbs.console.helper.EnumConsoleHelper;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.sms.message.outbox.model.SmsOutboxAttemptState;

@SingletonComponent ("smsOutboxAttemptStateConsoleHelper")
public
class SmsOutboxAttemptStateConsoleHelper
	extends EnumConsoleHelper<SmsOutboxAttemptState> {

	{

		enumClass (
			SmsOutboxAttemptState.class);

		auto ();

	}

}
