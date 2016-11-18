package wbs.sms.message.outbox.daemon;

import wbs.framework.logging.TaskLogger;

public
interface SmsSender {

	SmsSender smsMessageId (
			Long smsMessageId);

	void send (
			TaskLogger taskLogger);

}
