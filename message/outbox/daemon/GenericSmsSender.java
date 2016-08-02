package wbs.sms.message.outbox.daemon;

public
interface GenericSmsSender
	extends SmsSender {

	GenericSmsSender smsSenderHelper (
			SmsSenderHelper<?> smsSenderHelper);

}
