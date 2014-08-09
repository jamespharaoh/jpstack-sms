package wbs.sms.message.outbox.hibernate;

import wbs.framework.hibernate.EnumUserType;
import wbs.sms.message.outbox.model.SmsOutboxAttemptState;

public
class SmsOutboxAttemptStateType
	extends EnumUserType<String,SmsOutboxAttemptState> {

	{

		sqlType (1111);
		enumClass (SmsOutboxAttemptState.class);

		auto (String.class);

	}

}
