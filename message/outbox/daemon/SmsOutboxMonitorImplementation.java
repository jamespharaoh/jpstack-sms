package wbs.sms.message.outbox.daemon;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.keyEqualsDecimalInteger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
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
	void createThreads (
			@NonNull TaskLogger parentTaskLogger) {

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

			try (

				OwnedTaskLogger taskLogger =
					logContext.createTaskLogger (
						"runOnce");

			) {

				taskLogger.debugFormat (
					"Polling database");

				Map <Long, Long> routeSummaries =
					getRouteSummaries (
						taskLogger);

				synchronized (waitersLock) {

					routeSummaries.entrySet ().forEach (
						routeSummary ->
							doRouteSummary (
								taskLogger,
								routeSummary));

				}

			}

		}

		private
		Map <Long, Long> getRouteSummaries (
				@NonNull TaskLogger parentTaskLogger) {

			try (

				OwnedTransaction transaction =
					database.beginReadOnlyWithoutParameters (
						logContext,
						parentTaskLogger,
						"getRouteSummaries");

			) {

				return outboxHelper.generateRouteSummary (
					transaction,
					transaction.now ());

			}

		}

		private
		void doRouteSummary (
				@NonNull TaskLogger parentTaskLogger,
				@NonNull Map.Entry <Long, Long> routeSummary) {

			try (

				OwnedTaskLogger taskLogger =
					logContext.nestTaskLogger (
						parentTaskLogger,
						"doRouteSummary",
						keyEqualsDecimalInteger (
							"routeId",
							routeSummary.getKey ()));

			) {

				long routeId =
					routeSummary.getKey ();

				long count =
					routeSummary.getValue ();

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

	}

}
