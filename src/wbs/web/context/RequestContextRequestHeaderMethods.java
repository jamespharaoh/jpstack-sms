package wbs.web.context;

import static wbs.utils.etc.Misc.requiredValue;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.OptionalUtils.optionalCast;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalOrElseRequired;
import static wbs.utils.etc.NullUtils.isNull;

import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

public
interface RequestContextRequestHeaderMethods
	extends RequestContextCoreMethods {

	default
	Map <String, List <String>> headerMap () {

		State state =
			requestContextHeaderMethodsState ();

		if (
			isNull (
				state.headerMap)
		) {

			ImmutableMap.Builder <String, List <String>> headerMapBuilder =
				ImmutableMap.builder ();

			Enumeration <?> headerNamesEnumeration =
				request ().getHeaderNames ();

			while (
				headerNamesEnumeration.hasMoreElements ()
			) {

				String headerName =
					(String)
					headerNamesEnumeration.nextElement ();

				Enumeration<?> headerValuesEnumeration =
					request ().getHeaders (
						headerName);

				ImmutableList.Builder<String> headerValuesBuilder =
					ImmutableList.<String>builder ();

				while (
					headerValuesEnumeration.hasMoreElements ()
				) {

					String headerValue =
						(String)
						headerValuesEnumeration.nextElement ();

					headerValuesBuilder.add (
						headerValue);

				}

				headerMapBuilder.put (
					headerName,
					headerValuesBuilder.build ());

			}

			state.headerMap =
				headerMapBuilder.build ();

		}

		return state.headerMap;

	}

	default
	Optional <String> header (
			@NonNull String name) {

		return optionalFromNullable (
			request ().getHeader (
				name));

	}

	default
	String headerRequired (
			@NonNull String name) {

		return requiredValue (
			request ().getHeader (
				name));

	}

	default
	String headerOrEmptyString (
			@NonNull String name) {

		return ifNull (
			request ().getHeader (
				name),
			"");

	}

	default
	void setHeader (
			@NonNull String name,
			@NonNull String value) {

		response ().setHeader (
			name,
			value);

	}

	default
	void addHeader (
			@NonNull String name,
			@NonNull String value) {

		response ().addHeader (
			name,
			value);

	}

	// state

	final static
	String STATE_KEY =
		"REQUEST_CONTEXT_HEADER_METHODS_STATE";

	default
	State requestContextHeaderMethodsState () {

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
		Map <String, List <String>> headerMap;
	}

}
