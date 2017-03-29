package wbs.sms.message.outbox.daemon;

public
interface SmsOutboxMonitor {

	void waitForRoute (
			Long routeId)
		throws InterruptedException;

}
