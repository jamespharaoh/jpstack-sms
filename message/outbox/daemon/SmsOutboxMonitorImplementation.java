package wbs.sms.message.outbox.daemon;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.daemon.AbstractDaemonService;

import wbs.sms.message.outbox.model.OutboxObjectHelper;

/**
 * Daemon to periodicaly scan the entire outbox and gather statistics on the
 * messages ready for delivery, summarised by route. Clients can query the
 * number of messages waiting for a route and also wait for messages to become
 * ready for delivery.
 */
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

	@ClassSingletonDependency
	LogContext logContext;

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
			@NonNull Long routeId)
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

		countDownLatch.await ();

	}

	private
	class MonitorTask
		implements Runnable {

		@Override
		public
		void run () {

			for (;;) {

				// run once

				runOnce ();

				// sleep 1 interval

				try {

					Thread.sleep (
						sleepInterval);

				} catch (InterruptedException exception) {

					return;

				}

			}

		}

		public
		void runOnce () {

			TaskLogger taskLogger =
				logContext.createTaskLogger (
					"runOnce ()");

			taskLogger.debugFormat (
				"Polling database");

			// query database

			try (

				Transaction transaction =
					database.beginReadOnly (
						"SmsOutboxMonitorImplementation.runOnce ()",
						this);

			) {

				Map <Long, Long> routeSummary =
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

						taskLogger.debugFormat (
							"Route %s has %s messages",
							integerToDecimalString (
								routeId),
							integerToDecimalString (
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

			} catch (Exception exception) {

				// log error

				exceptionLogger.logThrowable (
					taskLogger,
					"daemon",
					"Outbox monitor",
					exception,
					Optional.absent (),
					GenericExceptionResolution.tryAgainLater);

			}

		}

	}

}
