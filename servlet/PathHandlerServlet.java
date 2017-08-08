package wbs.web.servlet;

import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.utils.string.StringUtils.joinWithoutSeparator;

import javax.servlet.ServletException;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NamedDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.web.exceptions.ExternalRedirectException;
import wbs.web.exceptions.HttpNotFoundException;
import wbs.web.file.WebFile;
import wbs.web.mvc.WebExceptionHandler;
import wbs.web.mvc.WebNotFoundHandler;
import wbs.web.pathhandler.PathHandler;

@SingletonComponent ("pathHandlerServlet")
public
class PathHandlerServlet
	extends WbsServlet {

	// singleton dependencies

	@SingletonDependency
	@NamedDependency
	PathHandler rootPathHandler;

	@SingletonDependency
	WebExceptionHandler exceptionHandler;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	WebNotFoundHandler notFoundHandler;

	// implementation

	@Override
	public
	void init ()
		throws ServletException {

		super.init ();

	}

	@Override
	protected
	WebFile processPath (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"processPath");

		) {

			// get the full path (servlet path & path info)

			String path =
				joinWithoutSeparator (
					requestContext.servletPath (),
					ifThenElse (
						optionalIsPresent (
							requestContext.pathInfo ()),
						() -> requestContext.pathInfo ().get (),
						() -> ""));

			// and call the relevant pathhandler

			return rootPathHandler.processPath (
				taskLogger,
				path);

		}

	}

	@Override
	protected
	void handleException (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Throwable exception) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"handleException");

		) {

			if (exception instanceof ExternalRedirectException) {

				ExternalRedirectException redirectException =
					(ExternalRedirectException) exception;

				requestContext.sendRedirect (
					redirectException.getLocation ());

			} else if (exception instanceof HttpNotFoundException) {

				throw (HttpNotFoundException) exception;

			} else if (exceptionHandler != null) {

				exceptionHandler.handleExceptionFinal (
					taskLogger,
					0l,
					exception);

			} else {

				super.handleException (
					taskLogger,
					exception);

			}

		}

	}

	@Override
	protected
	void handleNotFound (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"handleNotFound");

		) {

			if (notFoundHandler != null) {

				notFoundHandler.handleNotFound (
					parentTaskLogger);

			} else {

				super.handleNotFound (
					parentTaskLogger);

			}

		}

	}

}
