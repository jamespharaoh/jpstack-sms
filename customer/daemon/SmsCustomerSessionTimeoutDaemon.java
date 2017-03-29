package wbs.sms.customer.daemon;

import static wbs.utils.collection.IterableUtils.iterableMapToList;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;

import java.util.List;

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

import wbs.sms.customer.logic.SmsCustomerLogic;
import wbs.sms.customer.model.SmsCustomerManagerObjectHelper;
import wbs.sms.customer.model.SmsCustomerManagerRec;
import wbs.sms.customer.model.SmsCustomerSessionObjectHelper;
import wbs.sms.customer.model.SmsCustomerSessionRec;

import wbs.utils.time.TimeFormatter;

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

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	SmsCustomerLogic smsCustomerLogic;

	@SingletonDependency
	SmsCustomerManagerObjectHelper smsCustomerManagerHelper;

	@SingletonDependency
	SmsCustomerSessionObjectHelper smsCustomerSessionHelper;

	@SingletonDependency
	TimeFormatter timeFormatter;

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
	void runOnce (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"runOnce ()");

		try (

			Transaction transaction =
				database.beginReadOnly (
					"SmsCustomerSessionTimeoutDaemon.runOnce ()",
					this);

		) {

			List <Long> managerIds =
				iterableMapToList (
					SmsCustomerManagerRec::getId,
					smsCustomerManagerHelper.findAll ());

			transaction.close ();

			managerIds.forEach (
				managerId ->
					runOneManager (
						taskLogger,
						managerId));

		}

	}

	void runOneManager (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long managerId) {

		TaskLogger taskLogger =
			logContext.nestTaskLoggerFormat (
				parentTaskLogger,
				"runOneManager (%s)",
				integerToDecimalString (
					managerId));

		taskLogger.debugFormat (
			"Performing session timeouts for manager %s",
			integerToDecimalString (
				managerId));

		try (

			Transaction readTransaction =
				database.beginReadOnly (
					"SmsCustomerSessionTimeoutDaemon.runOneManager",
					this);

		) {

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

			taskLogger.debugFormat (
				"Got start time before %s",
				timeFormatter.timestampSecondStringIso (
					startTimeBefore));

			List <SmsCustomerSessionRec> sessionsToTimeout =
				smsCustomerSessionHelper.findToTimeoutLimit (
					manager,
					startTimeBefore,
					batchSize);

			taskLogger.debugFormat (
				"Found %s sessions",
				integerToDecimalString (
					sessionsToTimeout.size ()));

			for (
				SmsCustomerSessionRec session
					: sessionsToTimeout
			) {

				try (

					Transaction writeTransaction =
						database.beginReadWrite (
							"SmsCustomerSessionTimeoutDaemon.runOneManager (managerId)",
							this);

				) {

					session =
						smsCustomerSessionHelper.findRequired (
							session.getId ());

					smsCustomerLogic.sessionTimeoutAuto (
						taskLogger,
						session);

					writeTransaction.commit ();

				}

			}

		}

	}

}
