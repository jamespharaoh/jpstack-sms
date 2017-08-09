package wbs.sms.message.outbox.daemon;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.time.TimeUtils.isoTimestampString;

import java.util.List;

import lombok.NonNull;

import org.joda.time.Duration;
import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.daemon.SleepingDaemonService;

import wbs.sms.message.outbox.model.OutboxObjectHelper;
import wbs.sms.message.outbox.model.OutboxRec;

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

	// details

	@Override
	protected
	String friendlyName () {
		return "SMS outbox unstick";
	}

	@Override
	protected
	String backgroundProcessName () {
		return "outbox.unstick";
	}

	// implementation

	@Override
	protected
	void runOnce (
			@NonNull TaskLogger parentTaskLogger) {

		for (;;) {

			try (

				OwnedTransaction transaction =
					database.beginReadWrite (
						logContext,
						parentTaskLogger,
						"runOnce");

			) {

				Instant sendingBefore =
					transaction.now ().minus (
						timeoutDuration);

				List <OutboxRec> outboxesToUnstick =
					outboxHelper.findSendingBeforeLimit (
						transaction,
						sendingBefore,
						batchSize);

				if (outboxesToUnstick.isEmpty ())
					return;

				for (
					OutboxRec outbox
						: outboxesToUnstick
				) {

					transaction.warningFormat (
						"Unsticking outbox %s (sending time is %s)",
						integerToDecimalString (
							outbox.getId ()),
						isoTimestampString (
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

}
