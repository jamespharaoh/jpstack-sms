package wbs.web.context;

import static wbs.utils.etc.OptionalUtils.optionalCast;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.etc.OptionalUtils.optionalOrElseRequired;
import static wbs.utils.etc.TypeUtils.isNotInstanceOf;
import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.function.Supplier;

import com.google.common.base.Optional;

import lombok.NonNull;

public
interface RequestContextRequestAttributesMethods
	extends RequestContextCoreMethods {

	default
	void request (
			@NonNull String key,
			@NonNull Object value) {

		request ().setAttribute (
			key,
			value);

	}

	default
	Optional <Object> request (
			@NonNull String key) {

		return Optional.fromNullable (
			request ().getAttribute (
				key));

	}

	default
	Object requestRequired (
			@NonNull String key) {

		Object requestValue =
			request ().getAttribute (
				key);

		if (
			isNull (
				requestValue)
		) {

			throw new RuntimeException (
				stringFormat (
					"No such request attribute: %s",
					key));

		}

		return requestValue;

	}

	default
	Object requestOrElseSet (
			@NonNull String key,
			@NonNull Supplier <Object> supplier) {

		Optional <Object> valueOptional =
			request (
				key);

		if (
			optionalIsPresent (
				valueOptional)
		) {

			return optionalGetRequired (
				valueOptional);

		} else {

			Object value =
				supplier.get ();

			request (
				key,
				value);

			return value;

		}

	}

	default
	Optional <Long> requestInteger (
			@NonNull String key) {

		return optionalCast (
			Long.class,
			request (
				key));

	}

	default
	Long requestIntegerRequired (
			@NonNull String key) {

		Object requestValue =
			request ().getAttribute (
				key);

		if (
			isNull (
				requestValue)
		) {

			throw new RuntimeException (
				stringFormat (
					"No such request attribute: %s",
					key));

		}

		if (
			isNotInstanceOf (
				Long.class,
				requestValue)
		) {

			throw new RuntimeException (
				stringFormat (
					"Request attribute '%s' expected to be integer but is '%s'",
					key,
					requestValue.getClass ().getSimpleName ()));

		}

		return (Long)
			requestValue;

	}

	default
	Optional <String> requestString (
			@NonNull String key) {

		Object requestValue =
			request ().getAttribute (
				key);

		if (
			isNull (
				requestValue)
		) {

			return Optional.absent ();

		}

		if (
			isNotInstanceOf (
				String.class,
				requestValue)
		) {

			throw new RuntimeException (
				stringFormat (
					"Request attribute '%s' expected to be string but is '%s'",
					key,
					requestValue.getClass ().getSimpleName ()));

		}

		return Optional.of (
			(String)
			requestValue);

	}

	default
	String requestStringRequired (
			@NonNull String key) {

		Object requestValue =
			request ().getAttribute (
				key);

		if (
			isNull (
				requestValue)
		) {

			throw new RuntimeException (
				stringFormat (
					"No such request attribute: %s",
					key));

		}

		if (
			isNotInstanceOf (
				String.class,
				requestValue)
		) {

			throw new RuntimeException (
				stringFormat (
					"Request attribute '%s' expected to be string but is '%s'",
					key,
					requestValue.getClass ().getSimpleName ()));

		}

		return (String)
			requestValue;

	}

	// request unique

	default
	Long requestUnique () {

		State state =
			requestContextRequestAttributesMethodsState ();

		return state.nextRequestUnique ++;

	}

	// state

	final static
	String STATE_KEY =
		"REQUEST_CONTEXT_REQUEST_ATTRIBUTES_METHODS_STATE";

	default
	State requestContextRequestAttributesMethodsState () {

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
		Long nextRequestUnique = 0l;
	}

}
