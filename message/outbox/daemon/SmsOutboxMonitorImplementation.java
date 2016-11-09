package wbs.sms.message.outbox.daemon;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import com.google.common.base.Optional;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.platform.daemon.AbstractDaemonService;
import wbs.sms.message.outbox.model.OutboxObjectHelper;

/**
 * Daemon to periodicaly scan the entire outbox and gather statistics on the
 * messages ready for delivery, summarised by route. Clients can query the
 * number of messages waiting for a route and also wait for messages to become
 * ready for delivery.
 */
@Log4j
@SingletonComponent ("smsOutboxMonitor")
public
class SmsOutboxMonitorImplementation
	extends AbstractDaemonService
	implements SmsOutboxMonitor {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	ExceptionLogger exceptionLogger;

	@SingletonDependency
	OutboxObjectHelper outboxHelper;

	// details

	public final static
	long sleepInterval = 1000;

	@Override
	protected
	String getThreadName () {
		throw new UnsupportedOperationException ();
	}

	// state

	private
	Object waitersLock =
		new Object ();

	private
	Map<Long,CountDownLatch> waiters =
		new HashMap<Long,CountDownLatch> ();

	// implementation

	@Override
	protected
	void createThreads () {

		createThread (
			"OutboxMonitor",
			new MonitorTask ());

	}

	@Override
	public
	void waitForRoute (
			long routeId)
		throws InterruptedException {

		CountDownLatch countDownLatch;

		synchronized (waitersLock) {

			countDownLatch =
				waiters.get (
					routeId);

			if (countDownLatch == null) {

				countDownLatch =
					new CountDownLatch (1);

				waiters.put (
					routeId,
					countDownLatch);

			}

		}

		log.debug (
			stringFormat (
				"Thread %s is waiting for route %s",
				Thread.currentThread ().getName (),
				integerToDecimalString (
					routeId)));

		countDownLatch.await ();

	}

	private
	class MonitorTask
		implements Runnable {

		@Override
		public
		void run () {

			for (;;) {

				try {

					runOnce ();

					// sleep 1 interval

					try {

						Thread.sleep (
							sleepInterval);

					} catch (InterruptedException exception) {

						return;

					}

				} catch (Exception exception) {

					// log error

					exceptionLogger.logThrowable (
						"daemon",
						"Outbox monitor",
						exception,
						Optional.absent (),
						GenericExceptionResolution.tryAgainLater);

					// sleep 1 minute

					try {

						Thread.sleep (
							60 * 1000);

					} catch (InterruptedException exception2) {

						return;

					}

				}

			}

		}

		public
		void runOnce () {

			log.debug (
				"Polling database");

			// query database

			@Cleanup
			Transaction transaction =
				database.beginReadOnly (
					"SmsOutboxMonitorImplementation.runOnce ()",
					this);

			Map<Long,Long> routeSummary =
				outboxHelper.generateRouteSummary (
					transaction.now ());

			transaction.close ();

			// now set off and discard all affected latches

			synchronized (waitersLock) {

				for (
					Map.Entry<Long,Long> routeSummaryEntry
						: routeSummary.entrySet ()
				) {

					long routeId =
						routeSummaryEntry.getKey ();

					long count =
						routeSummaryEntry.getValue ();

					log.debug (
						stringFormat (
							"Route %s has %s messages",
							integerToDecimalString (
								routeId),
							integerToDecimalString (
								count)));

					CountDownLatch countDownLatch =
						waiters.get (
							routeId);

					if (countDownLatch != null) {

						countDownLatch.countDown ();

						waiters.remove (
							routeId);

					}

				}

			}

		}

	}

}
