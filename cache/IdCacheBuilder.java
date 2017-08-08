package wbs.utils.cache;

import static wbs.utils.etc.NullUtils.errorIfNull;

import java.util.function.BiFunction;
import java.util.function.Function;

import com.google.common.base.Optional;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.tuple.Pair;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsSpecialConfig;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.utils.etc.SafeCloseable;

@Accessors (fluent = true)
@Data
@PrototypeComponent ("idCacheBuilder")
public
class IdCacheBuilder <Context extends SafeCloseable, Key, Id, Value> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	WbsSpecialConfig wbsSpecialConfig;

	// protootype dependencies

	@PrototypeDependency
	ComponentProvider <IdLookupCache <Context, Key, Id, Value>>
		idLookupCacheProvider;

	// properties

	Boolean dummy = false;
	Boolean cacheNegatives = false;

	BiFunction <Context, Key, Optional <Value>> lookupByKeyFunction;
	BiFunction <Context, Id, Optional <Value>> lookupByIdFunction;
	Function <Value, Id> getIdFunction;
	BiFunction <Context, Key, Value> createFunction;
	BiFunction <Pair <LogContext, String>, Context, Context> wrapperFunction;

	// implementation

	public
	AdvancedCache <Context, Key, Value> build (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"build");

		) {

			if (dummy) {

				errorIfNull (
					taskLogger,
					"lookupBykeyFunction",
					lookupByKeyFunction);

				taskLogger.makeException ();

				return new DummyCache <Context, Key, Value> ()

					.lookupByKeyFunction (
						lookupByKeyFunction);

			} else {

				errorIfNull (
					taskLogger,
					"lookupBykeyFunction",
					lookupByKeyFunction);

				errorIfNull (
					taskLogger,
					"lookupByIdFunction",
					lookupByIdFunction);

				errorIfNull (
					taskLogger,
					"getIdFunction",
					getIdFunction);

				errorIfNull (
					taskLogger,
					"wrapperFunction",
					wrapperFunction);

				taskLogger.makeException ();

				return idLookupCacheProvider.provide (
					taskLogger,
					idLookupCache ->
						idLookupCache

					.assumeNegatives (
						wbsSpecialConfig.assumeNegativeCache ())

					.cacheNegatives (
						cacheNegatives)

					.lookupByIdFunction (
						lookupByIdFunction)

					.lookupByKeyFunction (
						lookupByKeyFunction)

					.getIdFunction (
						getIdFunction)

					.createFunction (
						createFunction)

					.wrapperFunction (
						wrapperFunction)

				);

			}

		}

	}

}
