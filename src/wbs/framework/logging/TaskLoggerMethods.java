package wbs.framework.logging;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public
interface TaskLoggerMethods
	extends TaskLoggerCoreMethods {

	FatalErrorException fatalFormat (
			CharSequence ... arguments);

	FatalErrorException fatalFormatException (
			Throwable exception,
			CharSequence ... arguments);

	void errorFormat (
			CharSequence ... arguments);

	LoggedErrorsException errorFormatThrow (
			CharSequence ... arguments);

	void errorFormatException (
			Throwable exception,
			CharSequence ... arguments);

	LoggedErrorsException errorFormatExceptionThrow (
			Throwable exception,
			CharSequence ... arguments);

	void firstErrorFormat (
			CharSequence ... arguments);

	void warningFormat (
			CharSequence ... arguments);

	void warningFormatException (
			Throwable exception,
			CharSequence ... arguments);

	void noticeFormat (
			CharSequence ... arguments);

	void noticeFormatException (
			Throwable exception,
			CharSequence ... arguments);

	void logicFormat (
			CharSequence ... arguments);

	void logicFormatException (
			Throwable exception,
			CharSequence ... arguments);

	void traceFormat (
			CharSequence ... arguments);

	void traceFormatException (
			Throwable exception,
			CharSequence ... arguments);

	void debugFormat (
			CharSequence ... arguments);

	void debugFormatException (
			Throwable exception,
			CharSequence ... arguments);

	RuntimeException makeException ();

	RuntimeException makeException (
			Supplier <RuntimeException> exceptionSupplier);

	boolean errors ();

	long errorCount ();

	<Type>
	Type wrap (
			Function <TaskLogger, Type> function);

	void wrap (
			Consumer <TaskLogger> function);

	BorrowedTaskLogger borrowTaskLogger ();

}
