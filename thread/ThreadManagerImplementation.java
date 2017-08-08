package wbs.utils.thread;

import static wbs.utils.etc.EnumUtils.enumEqualSafe;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.string.StringUtils.keyEqualsClassSimple;
import static wbs.utils.string.StringUtils.keyEqualsEnum;
import static wbs.utils.string.StringUtils.stringFormat;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.exception.ExceptionLogger;
import wbs.framework.exception.GenericExceptionResolution;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;

@Accessors (fluent = true)
@PrototypeComponent ("threadManagerImplementation")
public
class ThreadManagerImplementation
	implements ThreadManager {

	// singleton dependencies

	@SingletonDependency
	ExceptionLogger exceptionLogger;

	@ClassSingletonDependency
	LogContext logContext;

	// properties

	@Getter @Setter
	String exceptionTypeCode;

	// implementation

	/**
	 * Thread class which provides the desired functionality.
	 */
	class ManagedThread
		extends Thread {

		Runnable target;

		ManagedThread (
				Runnable target) {

			super ();

			if (target == null)
				throw new NullPointerException ();

			this.target =
				target;

		}

		ManagedThread (
				Runnable target,
				String name) {

			this (target);

			if (name != null)
				setName (name);

		}

		void logThrowable (
				@NonNull Throwable throwable,
				@NonNull GenericExceptionResolution resolution) {

			try (

				OwnedTaskLogger taskLogger =
					logContext.createTaskLogger (
						"logThrowable",
						keyEqualsClassSimple (
							"throwableClass",
							throwable.getClass ()),
						keyEqualsEnum (
							"resolution",
							resolution));

			) {

				if (
					enumEqualSafe (
						resolution,
						GenericExceptionResolution.fatalError)
				) {

					taskLogger.fatalFormatException (
						throwable,
						"Unhandled fatal exception in thread %s",
						getName ());

				} else {

					taskLogger.errorFormatException (
						throwable,
						"Unhandled exception in thread %s",
						getName ());

				}

				throwable.printStackTrace ();

				try {

					exceptionLogger.logThrowable (
						taskLogger,
						exceptionTypeCode,
						stringFormat (
							"Thread %s",
							getName ()),
						throwable,
						optionalAbsent (),
						resolution);

				} catch (Throwable exception) {

					taskLogger.fatalFormatException (
						exception,
						"Error logging exception in %s",
						getName ());

				}

			}

		}

		@Override
		public
		void run () {

			try {

				// start the thread

				target.run ();

			} catch (Throwable throwable) {

				logThrowable (
					throwable,
					GenericExceptionResolution.fatalError);

			}

		}

	}

	/**
	 * Create and return a managed thread, using the given Runnable as the
	 * target.
	 */
	@Override
	public
	Thread makeThread (
			@NonNull Runnable target) {

		return new ManagedThread (
			target);

	}

	/**
	 * Create, start and return a managed thread, using the given Runnable as
	 * the target.
	 */
	@Override
	public
	Thread startThread (
			@NonNull Runnable target) {

		Thread thread =
			makeThread (
				target);

		thread.start ();

		return thread;

	}

	/**
	 * Create and return a managed thread, using the given Runnable as the
	 * target, and with the given name.
	 */
	@Override
	public
	Thread makeThread (
			@NonNull Runnable target,
			@NonNull String name) {

		return new ManagedThread (
			target,
			name);

	}

	/**
	 * Create, start and return a managed thread, using the given Runnable as
	 * the target, and with the given name.
	 */
	@Override
	public
	Thread startThread (
			@NonNull Runnable target,
			@NonNull String name) {

		Thread thread =
			makeThread (
				target,
				name);

		thread.start ();

		return thread;

	}

	// thread factory

	@Override
	public
	Thread newThread (
			Runnable runnable) {

		return makeThread (
			runnable);

	}

}
