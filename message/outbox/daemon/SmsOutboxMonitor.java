package wbs.sms.message.outbox.daemon;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;

import com.google.common.base.Optional;

import wbs.framework.application.annotations.SingletonComponent;
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
@SingletonComponent ("outboxMonitor")
public
class SmsOutboxMonitor
	extends AbstractDaemonService {

	// dependencies

	@Inject
	Database database;

	@Inject
	ExceptionLogger exceptionLogger;

	@Inject
	OutboxObjectHelper outboxHelper;

	// details

	public final static
	int sleepInterval = 1000;

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
	Map<Integer,CountDownLatch> waiters =
		new HashMap<Integer,CountDownLatch> ();

	// implementation

	@Override
	protected
	void createThreads () {

		createThread (
			"OutboxMonitor",
			new MonitorTask ());

	}

	/**
	 * Blocks until a query shows messages ready to send on the given route.
	 *
	 * @param routeId
	 *            the route to wait for
	 */
	public
	void waitForRoute (
			int routeId)
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
				"Thread %s is waiting for route %d",
				Thread.currentThread ().getName (),
				routeId));

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
						Optional.<Integer>absent (),
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
					this);

			Map<Integer,Integer> routeSummary =
				outboxHelper.generateRouteSummary (
					transaction.now ());

			transaction.close ();

			// now set off and discard all affected latches

			synchronized (waitersLock) {

				for (
					Map.Entry<Integer,Integer> routeSummaryEntry
						: routeSummary.entrySet ()
				) {

					int routeId =
						routeSummaryEntry.getKey ();

					int count =
						routeSummaryEntry.getValue ();

					log.debug (
						stringFormat (
							"Route %d has %d messages",
							routeId,
							count));

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
