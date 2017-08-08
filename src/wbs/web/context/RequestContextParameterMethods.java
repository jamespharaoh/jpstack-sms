package wbs.web.context;

import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.collection.CollectionUtils.listFirstElementRequired;
import static wbs.utils.etc.NumberUtils.parseIntegerRequired;
import static wbs.utils.etc.OptionalUtils.optionalCast;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalMapRequiredOrDefault;
import static wbs.utils.etc.OptionalUtils.optionalOr;
import static wbs.utils.etc.OptionalUtils.optionalOrElseRequired;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;
import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.string.StringUtils.lowercase;
import static wbs.utils.string.StringUtils.stringEqualSafe;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import wbs.framework.logging.TaskLogger;

public
interface RequestContextParameterMethods
	extends RequestContextCoreMethods {

	default
	Map <String, List <String>> parameterMap () {

		State state =
			requestContextParameterMethodsState ();

		if (
			isNull (
				state.parameterMap)
		) {

			RequestContextMultipartMethods multipartMethods =
				genericCastUnchecked (
					this);

			ImmutableMap.Builder <String, List <String>> parameterMapBuilder =
				ImmutableMap.builder ();

			if (multipartMethods.isMultipart ()) {

				for (
					Map.Entry <String, String> fileItemFieldEntry
						: multipartMethods.fileItemFields ().entrySet ()
				) {

					String[] requestValues =
						request ().getParameterValues (
							fileItemFieldEntry.getKey ());

					parameterMapBuilder.put (
						fileItemFieldEntry.getKey (),
						ImmutableList.<String> builder ()

							.add (
								fileItemFieldEntry.getValue ())

							.add (
								requestValues != null
									? requestValues
									: new String [] {})

							.build ());

				}

			}

			for (
				Object entryObject
					: request ().getParameterMap ().entrySet ()
			) {

				Map.Entry <?,?> entry =
					(Map.Entry <?,?>) entryObject;

				String parameterName =
					(String) entry.getKey ();

				String[] parameterValuesArray =
					(String[]) entry.getValue ();

				if (

					multipartMethods.isMultipart ()

					&& multipartMethods.fileItemFields ().containsKey (
						parameterName)

				) {
					continue;
				}

				List <String> parameterValues =
					ImmutableList.copyOf (
						parameterValuesArray);

				parameterMapBuilder.put (
					parameterName,
					parameterValues);

			}

			state.parameterMap =
				parameterMapBuilder.build ();

		}

		return state.parameterMap;

	}

	default
	Map <String, String> parameterMapSimple () {

		State state =
			requestContextParameterMethodsState ();

		if (
			isNull (
				state.parameterMapSimple)
		) {

			ImmutableMap.Builder <String, String> builder =
				ImmutableMap.builder ();

			for (
				Map.Entry <String, List <String>> parametersEntry
					: parameterMap ().entrySet ()
			) {

				builder.put (
					parametersEntry.getKey (),
					parametersEntry.getValue ().get (0));

			}

			state.parameterMapSimple =
				builder.build ();

		}

		return state.parameterMapSimple;

	}

	default
	List <String> parameterValues (
			@NonNull String name) {

		return optionalOrElseRequired (
			optionalFromNullable (
				parameterMap ().get (
					name)),
			() -> emptyList ());

	}

	default
	Optional <String> parameter (
			@NonNull String key) {

		RequestContextMultipartMethods multipartMethods =
			genericCastUnchecked (
				this);

		if (

			multipartMethods.isMultipart ()

			&& multipartMethods.fileItemFields ().containsKey (
				key)

		) {

			return optionalFromNullable (
				multipartMethods.fileItemFields ().get (
					key));

		} else {

			return optionalFromNullable (
				request ().getParameter (
					key));

		}

	}


	default
	String parameterRequired (
			@NonNull String key) {

		return listFirstElementRequired (
			parameterValues (
				key));

	}

	@Deprecated
	default
	String parameterOrNull (
			String key) {

		return optionalOrNull (
			parameter (
				key));

	}

	default
	String parameterOrDefault (
			@NonNull String key,
			@NonNull String defaultValue) {

		return optionalOr (
			parameter (
				key),
			defaultValue);

	}

	default
	String parameterOrEmptyString (
			@NonNull String key) {

		return optionalOr (
			parameter (
				key),
			"");

	}

	default
	Long parameterIntegerRequired (
			@NonNull String key) {

		return parseIntegerRequired (
			parameterRequired (
				key));

	}

	default
	Boolean parameterOn (
			@NonNull String key) {

		return optionalMapRequiredOrDefault (
			value ->
				stringEqualSafe (
					lowercase (
						value),
					"on"),
			parameter (
				key),
			false);

	}

	default
	String parameterOrElse (
			@NonNull String key,
			@NonNull Supplier <String> orElse) {

		return optionalOrElseRequired (
			parameter (
				key),
			orElse);

	}

	default
	void debugParameters (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			parentTaskLogger;

		for (
			Map.Entry <String, List <String>> entry
				: parameterMap ().entrySet ()
		) {

			String name =
				entry.getKey ();

			for (
				String value
					: entry.getValue ()
			) {

				taskLogger.debugFormat (
					"Parameter: %s = \"%s\"",
					name,
					value);

			}

		}

	}

	// state

	final static
	String STATE_KEY =
		"REQUEST_CONTEXT_PARAMETER_METHODS_STATE";

	default
	State requestContextParameterMethodsState () {

		return optionalOrElseRequired (
			optionalCast (
				State.class,
				optionalFromNullable (
					request ().getAttribute (
						STATE_KEY))),
			() -> {

			State state =
				new State ();

			request ().setAttribute (
				STATE_KEY,
				state);

			return state;

		});

	}

	static
	class State {
		Map <String, List <String>> parameterMap;
		Map <String, String> parameterMapSimple;
	}

}
