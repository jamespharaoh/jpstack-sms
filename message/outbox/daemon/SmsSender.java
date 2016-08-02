package wbs.sms.message.outbox.daemon;

public
interface SmsSender {

	SmsSender smsMessageId (
			Long smsMessageId);

	void send ();

}
