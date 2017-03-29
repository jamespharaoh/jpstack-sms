package wbs.sms.message.outbox.daemon;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;

import java.util.List;

import lombok.Cleanup;
import lombok.NonNull;

import org.joda.time.Duration;
import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.daemon.SleepingDaemonService;

import wbs.sms.message.outbox.model.OutboxObjectHelper;
import wbs.sms.message.outbox.model.OutboxRec;

import wbs.utils.time.TimeFormatter;

@SingletonComponent ("messageOutboxUnstickDaemon")
public
class SmsOutboxUnstickDaemon
	extends SleepingDaemonService {

	// constants

	final
	long batchSize = 100;

	final
	Duration sleepDuration =
		Duration.standardSeconds (
			10);

	final
	Duration timeoutDuration =
		Duration.standardMinutes (
			5);

	// singleton dependencies

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	OutboxObjectHelper outboxHelper;

	@SingletonDependency
	TimeFormatter timeFormatter;

	// details

	@Override
	protected
	String getThreadName () {
		return "SmsOutboxUnstick";
	}

	@Override
	protected
	Duration getSleepDuration () {
		return sleepDuration;
	}

	@Override
	protected
	String generalErrorSource () {
		return "message outbox unstick daemon";
	}

	@Override
	protected
	String generalErrorSummary () {
		return "error checking for stuck message outboxes to unstick";
	}

	// implementation

	@Override
	protected
	void runOnce (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"runOnce ()");

		for (;;) {

			@Cleanup
			Transaction transaction =
				database.beginReadWrite (
					"SmsOutboxUnstickDaemon.runOnce ()",
					this);

			Instant sendingBefore =
				transaction.now ().minus (
					timeoutDuration);

			List<OutboxRec> outboxesToUnstick =
				outboxHelper.findSendingBeforeLimit (
					sendingBefore,
					batchSize);

			if (outboxesToUnstick.isEmpty ())
				return;

			for (
				OutboxRec outbox
					: outboxesToUnstick
			) {

				taskLogger.warningFormat (
					"Unsticking outbox %s (sending time is %s)",
					integerToDecimalString (
						outbox.getId ()),
					timeFormatter.timestampSecondStringIso (
						outbox.getSending ()));

				outbox

					.setSending (
						null)

					.setTries (
						outbox.getTries () + 1)

					.setError (
						"Send process never completed (stuck outbox)");

			}

			transaction.commit ();

		}

	}

}
