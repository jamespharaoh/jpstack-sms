package wbs.sms.customer.daemon;

import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.List;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.extern.log4j.Log4j;

import org.joda.time.Duration;
import org.joda.time.Instant;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
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

	public final static
	Duration sleepDuration =
		Duration.standardSeconds (
			10);

	public final static
	long batchSize = 100;

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	SmsCustomerLogic smsCustomerLogic;

	@SingletonDependency
	SmsCustomerManagerObjectHelper smsCustomerManagerHelper;

	@SingletonDependency
	SmsCustomerSessionObjectHelper smsCustomerSessionHelper;

	// details

	@Override
	protected
	String getThreadName () {
		return "SmsCustomerSessionTimeout";
	}

	@Override
	protected
	Duration getSleepDuration () {

		return sleepDuration;

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
				"SmsCustomerSessionTimeoutDaemon.runOnce ()",
				this);

		List<SmsCustomerManagerRec> managers =
			smsCustomerManagerHelper.findAll ();

		transaction.close ();

		for (
			SmsCustomerManagerRec manager
				: managers
		) {

			runOneManager (
				manager.getId ());

		}

	}

	void runOneManager (
			@NonNull Long managerId) {

		log.debug (
			stringFormat (
				"Performing session timeouts for manager %s",
				managerId));

		@Cleanup
		Transaction readTransaction =
			database.beginReadOnly (
				"SmsCustomerSessionTimeoutDaemon.runOneManager",
				this);

		SmsCustomerManagerRec manager =
			smsCustomerManagerHelper.findRequired (
				managerId);

		if (
			isNull (
				manager.getSessionTimeout ())
		) {
			return;
		}

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
			smsCustomerSessionHelper.findToTimeoutLimit (
				manager,
				startTimeBefore,
				batchSize);

		log.debug (
			stringFormat (
				"Found %s sessions",
				sessionsToTimeout.size ()));

		for (
			SmsCustomerSessionRec session
				: sessionsToTimeout
		) {

			@Cleanup
			Transaction writeTransaction =
				database.beginReadWrite (
					"SmsCustomerSessionTimeoutDaemon.runOneManager (managerId)",
					this);

			session =
				smsCustomerSessionHelper.findRequired (
					session.getId ());

			smsCustomerLogic.sessionTimeoutAuto (
				session);

			writeTransaction.commit ();

		}

	}

}
