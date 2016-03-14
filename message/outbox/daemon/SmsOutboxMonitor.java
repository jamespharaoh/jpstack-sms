package wbs.sms.message.outbox.daemon;

public
interface SmsOutboxMonitor {

	void waitForRoute (
			int routeId)
		throws InterruptedException;

}
