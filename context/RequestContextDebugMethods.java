package wbs.web.context;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.TypeUtils.genericCastUnchecked;

import java.util.Enumeration;
import java.util.Map;

import lombok.NonNull;

import org.apache.commons.fileupload.FileItem;

import wbs.framework.logging.TaskLogger;

public
interface RequestContextDebugMethods
	extends RequestContextCoreMethods {

	default
	void debugDump (
			@NonNull TaskLogger parentTaskLogger) {

		debugDump (
			parentTaskLogger,
			true);

	}

	default
	void debugDump (
			@NonNull TaskLogger parentTaskLogger,
			boolean doFiles) {

		TaskLogger taskLogger =
			parentTaskLogger;

		if (! taskLogger.debugEnabled ()) {
			return;
		}

		taskLogger.debugFormat (
			"REQUEST: %s %s",
			request ().getMethod (),
			request ().getRequestURI ());

		// output headers

		Enumeration <?> headersEnumeration =
			request ().getHeaderNames ();

		while (headersEnumeration.hasMoreElements ()) {

			String name =
				genericCastUnchecked (
					headersEnumeration.nextElement ());

			String value =
				request ().getHeader (
					name);

			taskLogger.debugFormat (
				"HEADER: %s = %s",
				name,
				value);

		}

		// output params

		for (
			Map.Entry <String, String[]> entry
				: request ().getParameterMap ().entrySet ()
		) {

			for (
				String value
					: entry.getValue ()
			) {

				taskLogger.debugFormat (
					"PARAM: %s = %s",
					entry.getKey (),
					value);

			}

		}

		// output files

		RequestContextMultipartMethods multipartMethods =
			genericCastUnchecked (
				this);

		if (doFiles && multipartMethods.isMultipart ()) {

			for (
				FileItem fileItem
					: multipartMethods.fileItems ()
			) {

				taskLogger.debugFormat (
					"FILE: %s = %s (%s)",
					fileItem.getFieldName (),
					fileItem.getContentType (),
					integerToDecimalString (
						fileItem.getSize ()));

			}

		}

	}

}
