package wbs.utils.collection;

import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import com.google.common.cache.LoadingCache;

import lombok.NonNull;

public
class CacheUtils {

	public static <Key, Value>
	Value cacheGet (
			@NonNull LoadingCache <Key, Value> loadingCache,
			@NonNull Key key) {

		try {

			return loadingCache.get (
				key);

		} catch (ExecutionException executionException) {

			throw new RuntimeException (
				executionException);

		}

	}

	public static <Key, Value>
	Value cacheGet (
			@NonNull LoadingCache <Key, Value> loadingCache,
			@NonNull Key key,
			@NonNull Supplier <? extends Value> supplier) {

		try {

			return loadingCache.get (
				key,
				() -> supplier.get ());

		} catch (ExecutionException executionException) {

			throw new RuntimeException (
				executionException);

		}

	}

}
