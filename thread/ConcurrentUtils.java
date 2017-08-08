package wbs.utils.thread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import lombok.NonNull;

public
class ConcurrentUtils {

	public static <Type>
	Future<Type> futureValue (
			@NonNull Type value) {

		CompletableFuture<Type> future =
			new CompletableFuture<> ();

		future.complete (
			value);

		return future;

	}

	public static <Type>
	Type futureGet (
			@NonNull Future<Type> value) {

		try {

			return value.get ();

		} catch (InterruptedException interruptedException) {

			throw new RuntimeException (
				interruptedException);

		} catch (ExecutionException executionException) {

			throw new RuntimeException (
				executionException);

		}

	}

}
