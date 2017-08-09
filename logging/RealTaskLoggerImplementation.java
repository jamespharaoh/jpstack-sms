package wbs.framework.logging;

import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.NumberUtils.equalToZero;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.moreThanZero;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalDo;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalMapRequiredOrDefault;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.string.StringUtils.joinWithCommaAndSpace;
import static wbs.utils.string.StringUtils.objectToString;
import static wbs.utils.string.StringUtils.stringFormat;
import static wbs.utils.string.StringUtils.stringFormatArray;
import static wbs.utils.string.StringUtils.stringFormatLazy;
import static wbs.utils.string.StringUtils.stringFormatLazyArray;
import static wbs.utils.time.TimeUtils.longerThan;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import lombok.NonNull;

import org.joda.time.Duration;
import org.joda.time.Instant;

import wbs.utils.string.LazyString;

public
class RealTaskLoggerImplementation
	implements
		OwnedTaskLogger,
		RealTaskLogger,
		TaskLoggerDefault {

	// state

	private final
	LoggingLogic loggingLogic;

	private final
	Long eventId;

	private final
	Optional <ParentTaskLogger> parentOptional;

	private final
	RealTaskLogger rootTaskLogger;

	private final
	LogTarget logTarget;

	private final
	String staticContextName;

	private final
	String dynamicContextName;

	private final
	List <CharSequence> dynamicContextParameters;

	private final
	long nesting;

	private final
	Boolean debugEnabled;

	private final
	Instant startTime;

	private
	Instant endTime;

	LogSeverity severity =
		LogSeverity.trace;

	long errorCount;
	long warningCount;
	long noticeCount;
	long logicCount;
	long debugCount;

	boolean addedToParent;

	CharSequence firstError;
	CharSequence lastError = "Aborting";

	List <String> errorMessages =
		new ArrayList<> ();

	List <TaskLogEvent> events =
		new ArrayList<> ();

	// constructors

	public
	RealTaskLoggerImplementation (
			@NonNull LoggingLogic loggingLogic,
			@NonNull Optional <ParentTaskLogger> parentOptional,
			@NonNull LogTarget logTarget,
			@NonNull String staticContextName,
			@NonNull String dynamicContextName,
			@NonNull List <CharSequence> dynamicContextParameters,
			@NonNull Optional <Boolean> debugEnabled) {

		this.loggingLogic =
			loggingLogic;

		this.eventId =
			loggingLogic.nextEventId ();

		this.parentOptional =
			parentOptional;

		this.rootTaskLogger =
			optionalMapRequiredOrDefault (
				ParentTaskLogger::getRoot,
				parentOptional,
				this);

		this.nesting =
			optionalMapRequiredOrDefault (
				parent ->
					parent.nesting () + 1l,
				parentOptional,
				0l);

		this.logTarget =
			logTarget;

		this.staticContextName =
			staticContextName;

		this.dynamicContextName =
			dynamicContextName;

		this.dynamicContextParameters =
			dynamicContextParameters;

		if (
			optionalIsPresent (
				debugEnabled)
		) {

			this.debugEnabled =
				optionalGetRequired (
					debugEnabled);

		} else {

			this.debugEnabled =
				optionalMapRequiredOrDefault (
					ParentTaskLogger::debugEnabled,
					parentOptional,
					false);

		}

		if (this.debugEnabled) {
			addToParent ();
		}

		this.startTime =
			Instant.now ();

		if (
			optionalIsNotPresent (
				parentOptional)
		) {

			loggingLogic.rootTaskBegin (
				this);

		}

		debugFormat (
			"Task logger %s started at %s",
			toStringLazy (),
			LazyString.singleton (
				() -> startTime.toString ()));

	}

	// life cycle

	@Override
	public
	void close () {

		synchronized (getRoot ()) {

			if (
				isNotNull (
					this.endTime)
			) {
				return;
			}

			Instant endTime =
				Instant.now ();

			if (
				optionalIsNotPresent (
					parentOptional)
			) {

				loggingLogic.rootTaskEnd (
					this);

			}

			debugFormat (
				"Task logger %s ended at %s",
				toStringLazy (),
				LazyString.singleton (
					() -> endTime.toString ()));

			this.endTime =
				endTime;

			if (
				longerThan (
					new Duration (
						this.startTime,
						this.endTime),
					Duration.millis (10))
			) {
				addToParent ();
			}

		}

	}

	// accessors

	@Override
	public
	RealTaskLogger getRoot () {
		return rootTaskLogger;
	}

	@Override
	public
	Long nesting () {
		return nesting;
	}

	@Override
	public
	Optional <ParentTaskLogger> parentOptional () {
		return parentOptional;
	}

	@Override
	public
	long errorCount () {

		synchronized (getRoot ()) {

			return errorCount;

		}

	}

	@Override
	public
	boolean errors () {

		synchronized (getRoot ()) {

			return moreThanZero (
				errorCount);

		}

	}

	@Override
	public
	void firstErrorFormat (
			@NonNull CharSequence ... arguments) {

		synchronized (getRoot ()) {

			firstError =
				stringFormatLazyArray (
					arguments);

		}

	}

	public
	void lastErrorFormat (
			@NonNull String ... arguments) {

		synchronized (getRoot ()) {

			lastError =
				stringFormatArray (
					arguments);

		}

	}

	@Override
	public
	void addChild (
			@NonNull OwnedTaskLogger child) {

		synchronized (getRoot ()) {

			if (
				isNotNull (
					endTime)
			) {

				throw new IllegalStateException (
					stringFormat (
						"Cannot add children to closed task logger %s",
						toString ()));

			}

			addToParent ();

			events.add (
				child);

		}

	}

	@Override
	public
	String toString () {

		synchronized (getRoot ()) {

			return stringFormat (
				"%s.%s (%s) (%s)",
				staticContextName,
				dynamicContextName,
				joinWithCommaAndSpace (
					dynamicContextParameters),
				integerToDecimalString (
					eventId));

		}

	}

	public
	LazyString toStringLazy () {

		return LazyString.singleton (
			() -> toString ());

	}

	// implementation

	public
	void logFormat (
			@NonNull LogSeverity severity,
			@NonNull String ... arguments) {

		switch (severity) {

		case fatal:

			throw fatalFormat (
				arguments);

		case error:

			errorFormat (
				arguments);

			break;

		case warning:

			warningFormat (
				arguments);

			break;

		case notice:

			noticeFormat (
				arguments);

			break;

		case logic:

			logicFormat (
				arguments);

			break;

		case trace:

			debugFormat (
				arguments);

			break;

		case debug:

			debugFormat (
				arguments);

			break;

		}

	}

	@Override
	public
	FatalErrorException fatalFormat (
			@NonNull CharSequence ... arguments) {

		synchronized (getRoot ()) {

			LazyString message =
				stringFormatLazyArray (
					arguments);

			logTarget.writeToLog (
				this,
				LogSeverity.fatal,
				message,
				optionalAbsent ());

			events.add (
				new TaskLogEntryEvent (
					loggingLogic.nextEventId (),
					LogSeverity.fatal,
					message));

			throw new FatalErrorException (
				this,
				message.toString ());

		}

	}

	@Override
	public
	FatalErrorException fatalFormatException (
			@NonNull Throwable throwable,
			@NonNull CharSequence ... arguments) {

		synchronized (getRoot ()) {

			LazyString message =
				stringFormatLazyArray (
					arguments);

			logTarget.writeToLog (
				this,
				LogSeverity.fatal,
				message,
				optionalOf (
					throwable));

			events.add (
				new TaskLogEntryEvent (
					loggingLogic.nextEventId (),
					LogSeverity.fatal,
					message));

			throw new FatalErrorException (
				this,
				message.toString (),
				throwable);

		}

	}

	@Override
	public
	void errorFormat (
			@NonNull CharSequence ... arguments) {

		synchronized (getRoot ()) {

			writeFirstError ();

			LazyString message =
				stringFormatLazyArray (
					arguments);

			logTarget.writeToLog (
				this,
				LogSeverity.error,
				message,
				optionalAbsent ());

			events.add (
				new TaskLogEntryEvent (
					loggingLogic.nextEventId (),
					LogSeverity.error,
					message));

			increaseErrorCount ();

		}

	}

	@Override
	public
	void errorFormatException (
			@NonNull Throwable throwable,
			@NonNull CharSequence ... arguments) {

		synchronized (getRoot ()) {

			writeFirstError ();

			LazyString message =
				stringFormatLazyArray (
					arguments);

			logTarget.writeToLog (
				this,
				LogSeverity.error,
				message,
				optionalOf (
					throwable));

			events.add (
				new TaskLogEntryEvent (
					loggingLogic.nextEventId (),
					LogSeverity.error,
					message));

			increaseErrorCount ();

		}

	}

	@Override
	public
	void warningFormat (
			@NonNull CharSequence ... arguments) {

		synchronized (getRoot ()) {

			LazyString message =
				stringFormatLazyArray (
					arguments);

			logTarget.writeToLog (
				this,
				LogSeverity.warning,
				message,
				optionalAbsent ());

			events.add (
				new TaskLogEntryEvent (
					loggingLogic.nextEventId (),
					LogSeverity.warning,
					message));

			increaseWarningCount ();

		}

	}

	@Override
	public
	void warningFormatException (
			@NonNull Throwable throwable,
			@NonNull CharSequence ... arguments) {

		synchronized (getRoot ()) {

			LazyString message =
				stringFormatLazyArray (
					arguments);

			logTarget.writeToLog (
				this,
				LogSeverity.warning,
				message,
				optionalOf (
					throwable));

			events.add (
				new TaskLogEntryEvent (
					loggingLogic.nextEventId (),
					LogSeverity.warning,
					message));

			increaseWarningCount ();

		}

	}

	@Override
	public
	void noticeFormat (
			@NonNull CharSequence ... arguments) {

		synchronized (getRoot ()) {

			LazyString message =
				stringFormatLazyArray (
					arguments);

			logTarget.writeToLog (
				this,
				LogSeverity.notice,
				message,
				optionalAbsent ());

			events.add (
				new TaskLogEntryEvent (
					loggingLogic.nextEventId (),
					LogSeverity.notice,
					message));

			increaseNoticeCount ();

		}

	}

	@Override
	public
	void noticeFormatException (
			@NonNull Throwable throwable,
			@NonNull CharSequence ... arguments) {

		synchronized (getRoot ()) {

			LazyString message =
				stringFormatLazyArray (
					arguments);

			logTarget.writeToLog (
				this,
				LogSeverity.notice,
				message,
				optionalOf (
					throwable));

			events.add (
				new TaskLogEntryEvent (
					loggingLogic.nextEventId (),
					LogSeverity.notice,
					message));

			increaseNoticeCount ();

		}

	}

	@Override
	public
	void logicFormat (
			@NonNull CharSequence ... arguments) {

		synchronized (getRoot ()) {

			LazyString message =
				stringFormatLazyArray (
					arguments);

			logTarget.writeToLog (
				this,
				LogSeverity.logic,
				message,
				optionalAbsent ());

			events.add (
				new TaskLogEntryEvent (
					loggingLogic.nextEventId (),
					LogSeverity.logic,
					message));

			increaseLogicCount ();

		}

	}

	@Override
	public
	void debugFormat (
			@NonNull CharSequence ... arguments) {

		synchronized (getRoot ()) {

			if (! debugEnabled ()) {
				return;
			}

			LazyString message =
				stringFormatLazyArray (
					arguments);

			logTarget.writeToLog (
				this,
				LogSeverity.debug,
				message,
				optionalAbsent ());

			events.add (
				new TaskLogEntryEvent (
					loggingLogic.nextEventId (),
					LogSeverity.debug,
					message));

			increaseDebugCount ();

		}

	}

	@Override
	public
	void debugFormatException (
			@NonNull Throwable throwable,
			@NonNull CharSequence ... arguments) {

		synchronized (getRoot ()) {

			if (! debugEnabled ()) {
				return;
			}

			LazyString message =
				stringFormatLazyArray (
					arguments);

			logTarget.writeToLog (
				this,
				LogSeverity.debug,
				message,
				optionalOf (
					throwable));

			events.add (
				new TaskLogEntryEvent (
					loggingLogic.nextEventId (),
					LogSeverity.debug,
					message));

			increaseDebugCount ();

		}

	}

	@Override
	public
	RuntimeException makeException (
			@NonNull Supplier <RuntimeException> exceptionSupplier) {

		synchronized (getRoot ()) {

			if (errors ()) {

				LazyString message =
					stringFormatLazy (
						"%s due to %s errors",
						lastError,
						integerToDecimalString (
							errorCount));

				logTarget.writeToLog (
					this,
					LogSeverity.error,
					message,
					optionalAbsent ());

				events.add (
					new TaskLogEntryEvent (
						loggingLogic.nextEventId (),
						LogSeverity.error,
						message));

				throw exceptionSupplier.get ();

			} else {

				return new RuntimeException (
					"No errors");

			}

		}

	}

	@Override
	public
	RuntimeException makeException () {

		synchronized (getRoot ()) {

			if (errors ()) {

				LazyString message =
					stringFormatLazy (
						"%s due to %s errors",
						lastError,
						integerToDecimalString (
							errorCount));

				logTarget.writeToLog (
					this,
					LogSeverity.error,
					message,
					optionalAbsent ());

				throw new LoggedErrorsException (
					this,
					message.toString ());

			} else {

				return new RuntimeException (
					"No errors");

			}

		}

	}

	@Override
	public <Type>
	Type wrap (
			@NonNull Function <TaskLogger, Type> function) {

		Type returnValue =
			function.apply (
				this);

		makeException ();

		return returnValue;

	}

	@Override
	public
	void wrap (
			@NonNull Consumer <TaskLogger> function) {

		function.accept (
			this);

		makeException ();

	}

	@Override
	public
	void writeFirstError () {

		synchronized (getRoot ()) {

			// recurse up through parents

			if (
				optionalIsPresent (
					parentOptional)
			) {

				parentOptional.get ().writeFirstError ();

			}

			if (

				equalToZero (
					errorCount)

				&& isNotNull (
					firstError)

			) {

				// add to parent if we didn't already

				addToParent ();

				// write first error

				logTarget.writeToLog (
					this,
					LogSeverity.error,
					firstError,
					optionalAbsent ());

				events.add (
					new TaskLogEntryEvent (
						loggingLogic.nextEventId (),
						LogSeverity.error,
						firstError));

			}

		}

	}

	@Override
	public
	void increaseErrorCount () {

		synchronized (getRoot ()) {

			errorCount ++;

			optionalDo (
				parentOptional,
				ParentTaskLogger::increaseErrorCount);

		}

	}

	@Override
	public
	void increaseWarningCount () {

		synchronized (getRoot ()) {

			warningCount ++;

			optionalDo (
				parentOptional,
				ParentTaskLogger::increaseWarningCount);

		}

	}

	@Override
	public
	void increaseNoticeCount () {

		synchronized (getRoot ()) {

			noticeCount ++;

			optionalDo (
				parentOptional,
				ParentTaskLogger::increaseNoticeCount);

		}

	}

	@Override
	public
	void increaseLogicCount () {

		synchronized (getRoot ()) {

			logicCount ++;

			optionalDo (
				parentOptional,
				ParentTaskLogger::increaseLogicCount);

		}

	}

	@Override
	public
	void increaseDebugCount () {

		synchronized (getRoot ()) {

			debugCount ++;

			optionalDo (
				parentOptional,
				ParentTaskLogger::increaseDebugCount);

		}

	}

	private
	void addToParent () {

		synchronized (getRoot ()) {

			if (addedToParent) {
				return;
			}

			if (
				optionalIsPresent (
					parentOptional)
			) {

				parentOptional.get ().addChild (
					this);

			}

			addedToParent = true;

		}

	}

	// task logger implementation

	@Override
	public
	Boolean debugEnabled () {

		return (
			logTarget.debugEnabled ()
			|| debugEnabled
		);

	}

	// task log event implementation

	@Override
	public
	Long eventId () {
		return eventId;
	}

	@Override
	public
	LogSeverity eventSeverity () {

		return severity;

	}

	@Override
	public
	String eventText () {

		synchronized (getRoot ()) {

			Instant endTime =
				ifNull (
					this.endTime,
					Instant.now ());

			return stringFormat (
				"%s.%s (%s) — %s (%s)",
				staticContextName,
				dynamicContextName,
				joinWithCommaAndSpace (
					dynamicContextParameters),
				objectToString (
					new Duration (
						startTime,
						endTime)),
				this.endTime != null
					? "complete"
					: "ongoing");

		}

	}

	@Override
	public
	Instant eventStartTime () {

		return startTime;

	}

	@Override
	public
	Instant eventEndTime () {

		return endTime;

	}

	@Override
	public
	List <TaskLogEvent> eventChildren () {

		synchronized (getRoot ()) {

			return ImmutableList.copyOf (
				events);

		}

	}

	@Override
	public
	RealTaskLogger realTaskLogger () {
		return this;
	}

	@Override
	public
	ParentTaskLogger parentTaskLogger () {
		return this;
	}

}
