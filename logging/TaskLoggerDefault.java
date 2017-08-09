package wbs.framework.logging;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import lombok.NonNull;

public
interface TaskLoggerDefault
	extends TaskLoggerMethods {

	@Override
	default
	FatalErrorException fatalFormat (
			@NonNull CharSequence ... arguments) {

		return realTaskLogger ().fatalFormat (
			arguments);

	}

	@Override
	default
	FatalErrorException fatalFormatException (
			@NonNull Throwable exception,
			@NonNull CharSequence ... arguments) {

		return realTaskLogger ().fatalFormatException (
			exception,
			arguments);

	}

	@Override
	default
	void errorFormat (
			@NonNull CharSequence ... arguments) {

		realTaskLogger ().errorFormat (
			arguments);

	}

	@Override
	default
	LoggedErrorsException errorFormatThrow (
			@NonNull CharSequence ... arguments) {

		realTaskLogger ().errorFormat (
			arguments);

		throw realTaskLogger ().makeException ();

	}

	@Override
	default
	void errorFormatException (
			@NonNull Throwable exception,
			@NonNull CharSequence ... arguments) {

		realTaskLogger ().errorFormatException (
			exception,
			arguments);

	}

	@Override
	default
	LoggedErrorsException errorFormatExceptionThrow (
			@NonNull Throwable exception,
			@NonNull CharSequence ... arguments) {

		realTaskLogger ().errorFormatException (
			exception,
			arguments);

		throw realTaskLogger ().makeException ();

	}

	@Override
	default
	void firstErrorFormat (
			@NonNull CharSequence ... arguments) {

		realTaskLogger ().firstErrorFormat (
			arguments);

	}

	@Override
	default
	void warningFormat (
			@NonNull CharSequence ... arguments) {

		realTaskLogger ().warningFormat (
			arguments);

	}

	@Override
	default
	void warningFormatException (
			@NonNull Throwable exception,
			@NonNull CharSequence ... arguments) {

		realTaskLogger ().warningFormatException (
			exception,
			arguments);

	}

	@Override
	default
	void noticeFormat (
			@NonNull CharSequence ... arguments) {

		realTaskLogger ().noticeFormat (
			arguments);

	}

	@Override
	default
	void noticeFormatException (
			@NonNull Throwable exception,
			@NonNull CharSequence ... arguments) {

		realTaskLogger ().noticeFormatException (
			exception,
			arguments);

	}

	@Override
	default
	void logicFormat (
			@NonNull CharSequence ... arguments) {

		realTaskLogger ().logicFormat (
			arguments);

	}

	@Override
	default
	void logicFormatException (
			@NonNull Throwable exception,
			@NonNull CharSequence ... arguments) {

		realTaskLogger ().logicFormatException (
			exception,
			arguments);

	}

	@Override
	default
	void traceFormat (
			@NonNull CharSequence ... arguments) {

		realTaskLogger ().traceFormat (
			arguments);

	}

	@Override
	default
	void traceFormatException (
			@NonNull Throwable exception,
			@NonNull CharSequence ... arguments) {

		realTaskLogger ().traceFormatException (
			exception,
			arguments);

	}

	@Override
	default
	void debugFormat (
			@NonNull CharSequence ... arguments) {

		realTaskLogger ().debugFormat (
			arguments);

	}

	@Override
	default
	void debugFormatException (
			@NonNull Throwable exception,
			@NonNull CharSequence ... arguments) {

		realTaskLogger ().debugFormatException (
			exception,
			arguments);

	}

	@Override
	default
	Boolean debugEnabled () {
		return realTaskLogger ().debugEnabled ();
	}

	@Override
	default
	RuntimeException makeException () {

		return realTaskLogger ().makeException ();

	}

	@Override
	default
	RuntimeException makeException (
			@NonNull Supplier <RuntimeException> exceptionSupplier) {

		return realTaskLogger ().makeException (
			exceptionSupplier);

	}

	@Override
	default
	RealTaskLogger getRoot () {

		return realTaskLogger ().getRoot ();

	}

	@Override
	default
	boolean errors () {

		return realTaskLogger ().errors ();

	}

	@Override
	default
	long errorCount () {

		return realTaskLogger ().errorCount ();

	}

	@Override
	default <Type>
	Type wrap (
			@NonNull Function <TaskLogger, Type> function) {

		return realTaskLogger ().wrap (
			function);

	}

	@Override
	default
	void wrap (
			@NonNull Consumer <TaskLogger> function) {

		realTaskLogger ().wrap (
			function);

	}

	@Override
	default
	BorrowedTaskLogger borrowTaskLogger () {

		return new BorrowedTaskLogger (
			this.realTaskLogger ());

	}

}
