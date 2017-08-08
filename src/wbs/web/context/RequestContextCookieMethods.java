package wbs.web.context;

import static wbs.utils.collection.MapUtils.mapItemForKey;
import static wbs.utils.collection.MapUtils.mapWithDerivedKey;
import static wbs.utils.etc.NullUtils.ifNull;
import static wbs.utils.etc.OptionalUtils.optionalCast;
import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalGetRequired;
import static wbs.utils.etc.OptionalUtils.optionalMapRequired;
import static wbs.utils.etc.OptionalUtils.optionalOrElseRequired;
import static wbs.utils.etc.NullUtils.isNull;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;

import com.google.common.base.Optional;

import lombok.NonNull;

public
interface RequestContextCookieMethods
	extends RequestContextCoreMethods {

	default
	Map <String, Cookie> cookiesByName () {

		State state =
			requestContextCookieMethodsState ();

		if (
			isNull (
				state.cookiesByName)
		) {

			state.cookiesByName =
				new HashMap<> (
					mapWithDerivedKey (
						ifNull (
							request ().getCookies (),
							new Cookie [] {}),
						Cookie::getName));

		}

		return state.cookiesByName;

	}

	default
	Optional <String> cookie (
			@NonNull String key) {

		return optionalMapRequired (
			mapItemForKey (
				cookiesByName (),
				key),
			Cookie::getValue);

	}

	default
	String cookieRequired (
			@NonNull String key) {

		return optionalGetRequired (
			cookie (
				key));

	}

	default
	void cookieSet (
			@NonNull String name,
			@NonNull String value) {

		State state =
			requestContextCookieMethodsState ();

		Cookie cookie =
			new Cookie (
				name,
				value);

		cookie.setPath (
			"/");

		state.cookiesByName.put (
			name,
			cookie);

		response ().addCookie (
			cookie);

	}

	default
	void cookieUnset (
			@NonNull String name) {

		State state =
			requestContextCookieMethodsState ();

		Cookie cookie =
			new Cookie (
				name,
				"");

		cookie.setPath (
			"/");

		cookie.setMaxAge (
			0);

		state.cookiesByName.put (
			name,
			cookie);

		response ().addCookie (
			cookie);

	}

	// state

	final static
	String STATE_KEY =
		"REQUEST_CONTEXT_COOKIE_METHODS_STATE";

	default
	State requestContextCookieMethodsState () {

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
		Map <String, Cookie> cookiesByName;
	}

}
