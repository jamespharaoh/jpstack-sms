package wbs.platform.scaffold.logic;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.etc.ThreadUtils.threadInterruptAndJoinIgnoreInterrupt;
import static wbs.utils.time.TimeUtils.laterThan;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.Instant;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NormalLifecycleSetup;
import wbs.framework.component.annotations.NormalLifecycleTeardown;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.tools.BackgroundProcess;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.platform.scaffold.model.SliceRec;

import wbs.utils.thread.ThreadManager;

@Accessors (fluent = true)
@SingletonComponent ("sliceLogic")
public
class SliceLogicImplementation
	implements
		BackgroundProcess,
		SliceLogic {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	ExceptionLogger exceptionLogger;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	SliceObjectHelper sliceHelper;

	@SingletonDependency
	ThreadManager threadManager;

	// properties

	@Getter @Setter
	Boolean runAutomatically = true;

	// state

	Map <Long, Optional <Instant>> nextTimestampBySlice =
		new HashMap<> ();

	Map <Long, Optional <Instant>> nextUpdateTimestampBySlice =
		new HashMap<> ();

	Thread backgroundThread;

	// lifecycle

	@NormalLifecycleSetup
	public
	void setup (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"setup");

		) {

			backgroundThread =
				threadManager.startThread (
					this::mainLoop,
					"SliceLogic");

		}

	}

	@NormalLifecycleTeardown
	public
	void teardown (
			@NonNull TaskLogger parentTaskLogger) {

		if (
			isNotNull (
				backgroundThread)
		) {

			threadInterruptAndJoinIgnoreInterrupt (
				backgroundThread);

		}

	}

	// implementation

	public
	void mainLoop () {

		for (;;) {

			try {

				Thread.sleep (
					1000);

				if (runAutomatically) {
					runOnce ();
				}

			} catch (InterruptedException exception) {

				return;

			}

		}

	}

	// implementation

	@Override
	public
	void runOnce () {

		try (

			OwnedTaskLogger taskLogger =
				logContext.createTaskLogger (
					"runOnce");

		) {

			runOnceReal (
				taskLogger);

		}

	}

	private synchronized
	void runOnceReal (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"runOnceReal");

		) {

			try {

				// iterate slices

				for (
					Long sliceId
						: nextUpdateTimestampBySlice.keySet ()
				) {

					SliceRec slice =
						sliceHelper.findRequired (
							transaction,
							sliceId);

					// perform update

					slice

						.setCurrentQueueInactivityTime (
							ifNull (
								slice.getCurrentQueueInactivityTime (),
								optionalOrNull (
									nextTimestampBySlice.get (
										sliceId))))

						.setCurrentQueueInactivityUpdateTime (
							optionalGetRequired (
								nextUpdateTimestampBySlice.get (
									sliceId)));

				}

				nextTimestampBySlice.clear ();
				nextUpdateTimestampBySlice.clear ();

				transaction.commit ();

			} catch (Exception exception) {

				exceptionLogger.logThrowable (
					transaction,
					"unknown",
					"sliceLogic",
					exception,
					optionalAbsent (),
					GenericExceptionResolution.tryAgainLater);

			}

		}

	}

	@Override
	public synchronized
	void updateSliceInactivityTimestamp (
			@NonNull Transaction parentTransaction,
			@NonNull SliceRec slice,
			@NonNull Optional <Instant> timestamp) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"updateSliceInactivitTimeout");

		) {

			Optional <Instant> nextUpdateTimestamp =
				nextUpdateTimestampBySlice.getOrDefault (
					slice.getId (),
					Optional.absent ());

			// skip if we have a more recent update

			if (

				optionalIsPresent (
					nextUpdateTimestamp)

				&& laterThan (
					transaction.now (),
					nextUpdateTimestamp.get ())

			) {
				return;
			}

			// remember this update

			nextUpdateTimestampBySlice.put (
				slice.getId (),
				Optional.of (
					transaction.now ()));

			nextTimestampBySlice.put (
				slice.getId (),
				timestamp);

		}

	}

}
