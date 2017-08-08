package wbs.utils.cache;

import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalMapOptional;
import static wbs.utils.etc.OptionalUtils.optionalOf;

import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.tuple.Pair;

import org.joda.time.Duration;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.logging.LogContext;

import wbs.utils.etc.SafeCloseable;

@PrototypeComponent ("idLookupCache")
@Accessors (fluent = true)
public
class IdLookupCache <Context extends SafeCloseable, Key, Id, Value>
	implements AdvancedCache <Context, Key, Value> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// constants

	public final static
	Duration keepDuration =
		Duration.standardMinutes (
			60);

	public final static
	Long maximumCacheSize =
		1024l * 1024l;

	// properties

	@Setter
	Boolean assumeNegatives;

	@Setter
	Boolean cacheNegatives;

	@Setter
	BiFunction <Context, Key, Optional <Value>> lookupByKeyFunction;

	@Setter
	BiFunction <Context, Id, Optional <Value>> lookupByIdFunction;

	@Setter
	Function <Value, Id> getIdFunction;

	@Setter
	BiFunction <Context, Key, Value> createFunction;

	@Setter
	BiFunction <Pair <LogContext, String>, Context, Context> wrapperFunction;

	// state

	@SuppressWarnings ("unchecked")
	Cache <Key, Optional <Id>> idCache =

		(Cache <Key, Optional <Id>>)

		(Cache <?, ?>)

		CacheBuilder.newBuilder ()

		.maximumSize (
			maximumCacheSize)

		.expireAfterAccess (
			keepDuration.getMillis (),
			TimeUnit.MILLISECONDS)

		.build ();

	// public implementation

	@Override
	public
	Optional <Value> find (
			@NonNull Context parentContext,
			@NonNull Key key) {

		try (

			Context context =
				wrapperFunction.apply (
					Pair.of (logContext, "find"),
					parentContext);

		) {

			// first try the cache

			Optional <Id> cachedIdOptional =
				idCache.getIfPresent (
					key);

			if (
				isNotNull (
					cachedIdOptional)
			) {

				return optionalMapOptional (
					cachedIdOptional,
					cachedId ->
						lookupByIdFunction.apply (
							context,
							cachedId));

			} else if (assumeNegatives) {

				return optionalAbsent ();

			}

			// lookup by key and store in cache

			Optional <Value> valueOptional =
				lookupByKeyFunction.apply (
					context,
					key);

			if (cacheNegatives) {

				idCache.put (
					key,
					optionalMapOptional (
						valueOptional,
						value ->
							Optional.of (
								getIdFunction.apply (
									value))));

			} else if (
				optionalIsPresent (
					valueOptional)
			) {

				idCache.put (
					key,
					Optional.of (
						getIdFunction.apply (
							valueOptional.get ())));

			}

			return valueOptional;

		}

	}

	@Override
	public
	Value create (
			@NonNull Context parentContext,
			@NonNull Key key) {

		try (

			Context context =
				wrapperFunction.apply (
					Pair.of (logContext, "create"),
					parentContext);

		) {

			Value value =
				createFunction.apply (
					context,
					key);

			Id id =
				getIdFunction.apply (
					value);

			idCache.put (
				key,
				optionalOf (
					id));

			return value;

		}

	}

	@Override
	public
	Value findOrCreate (
			@NonNull Context parentContext,
			@NonNull Key key) {

		try (

			Context context =
				wrapperFunction.apply (
					Pair.of (logContext, "findOrCreate"),
					parentContext);

		) {

			Optional <Value> valueOptional =
				find (
					context,
					key);

			if (
				optionalIsPresent (
					valueOptional)
			) {

				return optionalGetRequired (
					valueOptional);

			} else {

				return create (
					context,
					key);

			}

		}

	}

}
