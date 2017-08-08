package wbs.utils.collection;

import java.util.concurrent.ExecutionException;

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

}
