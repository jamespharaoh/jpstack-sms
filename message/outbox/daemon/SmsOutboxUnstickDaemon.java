package wbs.sms.message.outbox.daemon;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;

import org.joda.time.Instant;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.daemon.SleepingDaemonService;
import wbs.sms.message.outbox.model.OutboxObjectHelper;
import wbs.sms.message.outbox.model.OutboxRec;

@Log4j
@SingletonComponent ("messageOutboxUnstickDaemon")
public
class SmsOutboxUnstickDaemon
	extends SleepingDaemonService {

	// constants

	final
	int delayInSeconds = 10;

	final
	int batchSize = 100;

	final
	int timeoutMinutes = 5;

	// dependencies

	@Inject
	Database database;

	@Inject
	OutboxObjectHelper outboxHelper;

	// details

	@Override
	protected
	String getThreadName () {
		return "SmsOutboxUnstick";
	}

	@Override
	protected
	int getDelayMs () {
		return 1000 * delayInSeconds;
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
	void runOnce () {

		for (;;) {

			@Cleanup
			Transaction transaction =
				database.beginReadWrite (
					"SmsOutboxUnstickDaemon.runOnce ()",
					this);

			Instant sendingBefore =
				transaction.now ()
					.toDateTime ()
					.minusMinutes (timeoutMinutes)
					.toInstant ();

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

				log.warn (
					stringFormat (
						"Unsticking outbox %s (sending time is %s",
						outbox.getId (),
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
