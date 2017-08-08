package wbs.web.context;

import static wbs.utils.string.StringUtils.joinWithoutSeparator;
import static wbs.utils.string.StringUtils.stringFormatArray;

import java.io.InputStream;

import javax.servlet.RequestDispatcher;

import lombok.NonNull;

public
interface RequestContextContextMethods
	extends RequestContextCoreMethods {

	default
	Object context (
			@NonNull String key) {

		return context ().getAttribute (
			key);

	}

	default
	String applicationPathPrefix () {
		return request ().getContextPath ();
	}

	default
	String realPath (
			@NonNull String path) {

		return context ().getRealPath (
			path);

	}

	default
	String resolveApplicationUrl (
			@NonNull String applicationUrl) {

		return joinWithoutSeparator (
			applicationPathPrefix (),
			applicationUrl);

	}

	default
	String resolveApplicationUrlFormat (
			@NonNull String ... arguments) {

		return joinWithoutSeparator (
			applicationPathPrefix (),
			stringFormatArray (
				arguments));

	}

	default
	InputStream resourceAsStream (
			@NonNull String path) {

		return context ().getResourceAsStream (
			path);

	}

	default
	RequestDispatcher requestDispatcher (
			@NonNull String path) {

		return request ().getRequestDispatcher (
			path);

	}

}
