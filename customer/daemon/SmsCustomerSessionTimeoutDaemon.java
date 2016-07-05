package wbs.sms.customer.daemon;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.List;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import org.joda.time.Duration;
import org.joda.time.Instant;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.daemon.SleepingDaemonService;
import wbs.sms.customer.logic.SmsCustomerLogic;
import wbs.sms.customer.model.SmsCustomerManagerObjectHelper;
import wbs.sms.customer.model.SmsCustomerManagerRec;
import wbs.sms.customer.model.SmsCustomerSessionObjectHelper;
import wbs.sms.customer.model.SmsCustomerSessionRec;

@Log4j
@SingletonComponent ("smsCustomerSessionTimeoutDaemon")
public
class SmsCustomerSessionTimeoutDaemon
	extends SleepingDaemonService {

	// constants

	final
	int delayInSeconds = 10;

	final
	int batchSize = 100;

	// dependencies

	@Inject
	Database database;

	@Inject
	SmsCustomerLogic smsCustomerLogic;

	@Inject
	SmsCustomerManagerObjectHelper smsCustomerManagerHelper;

	@Inject
	SmsCustomerSessionObjectHelper smsCustomerSessionHelper;

	// details

	@Override
	protected
	String getThreadName () {
		return "SmsCustomerSessionTimeout";
	}

	@Override
	protected
	int getDelayMs () {
		return 1000 * delayInSeconds;
	}

	@Override
	protected
	String generalErrorSource () {
		return "sms customer session timeout daemon";
	}

	@Override
	protected
	String generalErrorSummary () {
		return "error checking for sms customer sessions to timeout";
	}

	// implementation

	@Override
	protected
	void runOnce () {

		@Cleanup
		Transaction transaction =
			database.beginReadOnly (
				this);

		List<SmsCustomerManagerRec> managers =
			smsCustomerManagerHelper.findAll ();

		transaction.close ();

		for (SmsCustomerManagerRec manager
				: managers) {

			runOneManager (
				manager.getId ());

		}

	}

	void runOneManager (
			@NonNull Integer managerId) {

		log.debug (
			stringFormat (
				"Performing session timeouts for manager %s",
				managerId));

		@Cleanup
		Transaction readTransaction =
			database.beginReadOnly (
				this);

		SmsCustomerManagerRec manager =
			smsCustomerManagerHelper.findOrNull (
				managerId);

		if (manager.getSessionTimeout () == null)
			return;

		Instant startTimeBefore =
			readTransaction.now ()
				.minus (
					Duration.standardSeconds (
						manager.getSessionTimeout ()));

		log.debug (
			stringFormat (
				"Got start time before %s",
				startTimeBefore));

		List<SmsCustomerSessionRec> sessionsToTimeout =
			smsCustomerSessionHelper.findToTimeout (
				manager,
				startTimeBefore,
				batchSize);

		log.debug (
			stringFormat (
				"Found %s sessions",
				sessionsToTimeout.size ()));

		for (SmsCustomerSessionRec session
				: sessionsToTimeout) {

			@Cleanup
			Transaction writeTransaction =
				database.beginReadWrite (
					this);

			session =
				smsCustomerSessionHelper.findOrNull (
					session.getId ());

			smsCustomerLogic.sessionTimeoutAuto (
				session);

			writeTransaction.commit ();

		}

	}

}
