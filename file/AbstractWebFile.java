package wbs.web.file;

import static wbs.utils.etc.NumberUtils.fromJavaInteger;

import javax.servlet.http.HttpServletResponse;

import lombok.NonNull;

import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.TaskLogger;

import wbs.web.context.RequestContext;

public
class AbstractWebFile
	implements WebFile {

	// singleton dependencies

	@SingletonDependency
	RequestContext requestContext;

	// implementation

	@Override
	public
	void doGet (
			@NonNull TaskLogger parentTaskLogger) {

		requestContext.sendError (
			fromJavaInteger (
				HttpServletResponse.SC_METHOD_NOT_ALLOWED),
			requestContext.requestUri ());

	}

	@Override
	public
	void doPost (
			@NonNull TaskLogger parentTaskLogger) {

		requestContext.sendError (
			fromJavaInteger (
				HttpServletResponse.SC_METHOD_NOT_ALLOWED),
			requestContext.requestUri ());

	}

	@Override
	public
	void doOptions (
			@NonNull TaskLogger parentTaskLogger) {

	}

}
