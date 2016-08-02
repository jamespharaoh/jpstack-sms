package wbs.sms.message.outbox.daemon;

public
interface SmsOutboxMonitor {

	void waitForRoute (
			long routeId)
		throws InterruptedException;

}
