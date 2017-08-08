package wbs.web.servlet;

import static wbs.utils.etc.NumberUtils.fromJavaInteger;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.manager.ComponentManager;
import wbs.framework.logging.BorrowedTaskLogger;
import wbs.framework.logging.CloseableTaskLogger;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.utils.etc.ImplicitArgument.BorrowedArgument;

import wbs.web.context.RequestContext;
import wbs.web.exceptions.ExternalRedirectException;
import wbs.web.exceptions.HttpForbiddenException;
import wbs.web.exceptions.HttpMethodNotAllowedException;
import wbs.web.file.WebFile;

public abstract
class WbsServlet
	extends HttpServlet {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// state

	protected
	ComponentManager componentManager;

	protected
	RequestContext requestContext;

	protected
	void doGet () {

		try (

			BorrowedArgument <CloseableTaskLogger, BorrowedTaskLogger>
				parentTaskLogger =
					TaskLogger.implicitArgument.borrow ();

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger.get (),
					"doGet");

		) {

			try {

				WebFile file =
					processPath (
						taskLogger);

				if (file != null) {

					file.doGet (
						taskLogger);

				} else {

					handleNotFound (
						taskLogger);

				}

			} catch (Throwable throwable) {

				handleException (
					taskLogger,
					throwable);

			}

		}

	}

	protected
	void doPost () {

		try (

			BorrowedArgument <CloseableTaskLogger, BorrowedTaskLogger>
				parentTaskLogger =
					TaskLogger.implicitArgument.borrow ();

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger.get (),
					"doPost");

		) {

			try {

				WebFile file =
					processPath (
						taskLogger);

				if (file != null) {

					file.doPost (
						taskLogger);

				} else {

					handleNotFound (
						taskLogger);

				}

			} catch (Throwable exception) {

				handleException (
					taskLogger,
					exception);

			}

		}

	}

	protected
	void doOptions () {

		try (

			BorrowedArgument <CloseableTaskLogger, BorrowedTaskLogger>
				parentTaskLogger =
					TaskLogger.implicitArgument.borrow ();

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger.get (),
					"doOptions");

		) {

			try {

				WebFile file =
					processPath (
						taskLogger);

				if (file != null) {

					file.doOptions (
						taskLogger);

				} else {

					handleNotFound (
						taskLogger);

				}

			} catch (Throwable exception) {

				handleException (
					taskLogger,
					exception);

			}

		}

	}

	@Override
	protected final
	void doGet (
			HttpServletRequest request,
			HttpServletResponse response)
		throws
			ServletException,
			IOException {

		doGet ();

	}

	@Override
	protected
	final void doPost (
			HttpServletRequest request,
			HttpServletResponse response)
		throws
			ServletException,
			IOException {

		doPost ();

	}

	@Override
	protected
	final void doOptions (
			HttpServletRequest request,
			HttpServletResponse response)
		throws
			ServletException,
			IOException {

		doOptions ();

	}

	protected
	WebFile processPath (
			@NonNull TaskLogger parentTaskLogger)
		throws ServletException {

		return null;

	}

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

			if (exception instanceof HttpMethodNotAllowedException) {

				handleMethodNotAllowed (
					taskLogger);

			} else if (exception instanceof HttpForbiddenException) {

				handleForbidden (
					taskLogger,
					requestContext);

			} else if (exception instanceof ExternalRedirectException) {

				ExternalRedirectException redirectException =
					(ExternalRedirectException)
					exception;

				requestContext.sendRedirect (
					redirectException.getLocation ());

			} else if (exception instanceof RuntimeException) {

				throw (RuntimeException)
					exception;

			} else {

				throw new RuntimeException (
					exception);

			}

		}

	}

	protected
	void handleNotFound (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"handleNotFound");

		) {

			taskLogger.warningFormat (
				"RCX not found %s",
				requestContext.requestUri ());

			requestContext.sendError (
				fromJavaInteger (
					HttpServletResponse.SC_NOT_FOUND),
				requestContext.requestUri ());

		}

	}

	protected
	void handleMethodNotAllowed (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"handleMethodNotAllowed");

		) {

			taskLogger.warningFormat (
				"method not allowed %s",
				requestContext.requestUri ());

			requestContext.sendError (
				fromJavaInteger (
					HttpServletResponse.SC_METHOD_NOT_ALLOWED));

		}

	}

	protected
	void handleForbidden (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull RequestContext requestContext) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"handleForbidden");

		) {

			taskLogger.warningFormat (
				"RCX forbidden %s",
				requestContext.requestUri ());

			requestContext.sendError (
				fromJavaInteger (
					HttpServletResponse.SC_FORBIDDEN));

		}

	}

	@Override
	public
	void init ()
		throws ServletException {

		try (

			OwnedTaskLogger taskLogger =
				logContext.createTaskLogger (
					"init");

		) {

			ServletContext servletContext =
				getServletContext ();

			componentManager =
				(ComponentManager)
				servletContext.getAttribute (
					"wbs-application-context");

			if (componentManager == null) {

				throw new ServletException (
					"Application context not found");

			}

			requestContext =
				componentManager.getComponentRequired (
					taskLogger,
					"requestContext",
					RequestContext.class);

			taskLogger.makeException ();

		}

	}

}
