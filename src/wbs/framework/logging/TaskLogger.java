package wbs.framework.logging;

import java.util.List;

import org.joda.time.Instant;

import wbs.utils.etc.ImplicitArgument;

public
interface TaskLogger
	extends
		TaskLogEvent,
		TaskLoggerMethods {

	// task log event interface

	@Override
	default
	Long eventId () {
		return realTaskLogger ().eventId ();
	}

	@Override
	default
	LogSeverity eventSeverity () {
		return realTaskLogger ().eventSeverity ();
	}

	@Override
	default
	String eventText () {
		return realTaskLogger ().eventText ();
	}

	@Override
	default
	Instant eventStartTime () {
		return realTaskLogger ().eventStartTime ();
	}

	@Override
	default
	Instant eventEndTime () {
		return realTaskLogger ().eventEndTime ();
	}

	@Override
	default
	List <TaskLogEvent> eventChildren () {
		return realTaskLogger ().eventChildren ();
	}

	// implicit argument

	ImplicitArgument <CloseableTaskLogger, BorrowedTaskLogger>
		implicitArgument =
			new ImplicitArgument <> (
				TaskLogger::borrowTaskLogger);

}
