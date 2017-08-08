package wbs.web.servlet;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;
import wbs.framework.servlet.ComponentFilterChain;
import wbs.framework.servlet.FilterComponent;

import wbs.web.context.RequestContextImplementation;

@SingletonComponent ("responseFilter")
public
class ResponseFilter
	implements FilterComponent {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// implementation

	@Override
	public
	void doFilter (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull ServletRequest request,
			@NonNull ServletResponse response,
			@NonNull ComponentFilterChain chain) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"doFilter");

		) {

			RequestContextImplementation.servletResponseThreadLocal.set (
				(HttpServletResponse)
				response);

			chain.doFilter (
				taskLogger,
				request,
				response);

		}

	}

}
