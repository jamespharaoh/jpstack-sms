package wbs.sms.customer.daemon;

import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.collection.IterableUtils.iterableMapToList;
import static wbs.utils.etc.NullUtils.isNull;
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
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.platform.daemon.SleepingDaemonService;

import wbs.sms.customer.logic.SmsCustomerLogic;
import wbs.sms.customer.model.SmsCustomerManagerObjectHelper;
import wbs.sms.customer.model.SmsCustomerManagerRec;
import wbs.sms.customer.model.SmsCustomerSessionObjectHelper;
import wbs.sms.customer.model.SmsCustomerSessionRec;

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

	// details

	@Override
	protected
	String friendlyName () {
		return "SMS customer session timeout";
	}

	@Override
	protected
	String backgroundProcessName () {
		return "sms-customer.session-timeout";
	}

	// implementation

	@Override
	protected
	void runOnce (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"runOnce");

		) {

			List <Long> managerIds =
				getManagerIds (
					taskLogger);

			managerIds.forEach (
				managerId ->
					doManager (
						taskLogger,
						managerId));

		}

	}

	List <Long> getManagerIds (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadOnly (
					logContext,
					parentTaskLogger,
					"getManagerIds");

		) {

			return iterableMapToList (
				smsCustomerManagerHelper.findAll (
					transaction),
				SmsCustomerManagerRec::getId);

		}

	}

	private
	void doManager (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long managerId) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"doManager");

		) {

			taskLogger.debugFormat (
				"Performing session timeouts for manager %s",
				integerToDecimalString (
					managerId));

			List <Long> sessionIds =
				getSessionIds (
					taskLogger,
					managerId);

			sessionIds.forEach (
				sessionId ->
					doSession (
						taskLogger,
						sessionId));

		}

	}

	private
	List <Long> getSessionIds (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long managerId) {

		try (

			OwnedTransaction transaction =
				database.beginReadOnly (
					logContext,
					parentTaskLogger,
					"getSessionIds");

		) {

			SmsCustomerManagerRec manager =
				smsCustomerManagerHelper.findRequired (
					transaction,
					managerId);

			if (
				isNull (
					manager.getSessionTimeout ())
			) {
				return emptyList ();
			}

			Instant startTimeBefore =
				transaction.now ().minus (
					Duration.standardSeconds (
						manager.getSessionTimeout ()));

			transaction.debugFormat (
				"Got start time before %s",
				isoTimestampString (
					startTimeBefore));

			List <SmsCustomerSessionRec> sessionsToTimeout =
				smsCustomerSessionHelper.findToTimeoutLimit (
					transaction,
					manager,
					startTimeBefore,
					batchSize);

			transaction.debugFormat (
				"Found %s sessions",
				integerToDecimalString (
					sessionsToTimeout.size ()));

			return iterableMapToList (
				sessionsToTimeout,
				SmsCustomerSessionRec::getId);

		}

	}

	private
	void doSession (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Long sessionId) {

		try (

			OwnedTransaction transaction =
				database.beginReadOnly (
					logContext,
					parentTaskLogger,
					"doSession");

		) {

			SmsCustomerSessionRec session =
				smsCustomerSessionHelper.findRequired (
					transaction,
					sessionId);

			smsCustomerLogic.sessionTimeoutAuto (
				transaction,
				session);

			transaction.commit ();

		}

	}

}
