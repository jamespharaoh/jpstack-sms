package wbs.web.context;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ProxiedRequestComponent;

@Accessors (fluent = true)
@ProxiedRequestComponent (
	value = "requestContext",
	proxyInterface = RequestContext.class)
public
class RequestContextImplementation
	implements RequestContext {

	public static
	ThreadLocal <ServletContext> servletContextThreadLocal =
		new ThreadLocal<> ();

	public static
	ThreadLocal <HttpServletRequest> servletRequestThreadLocal =
		new ThreadLocal<> ();

	public static
	ThreadLocal <HttpServletResponse> servletResponseThreadLocal =
		new ThreadLocal<> ();

	@Override
	public
	HttpServletRequest request () {
		return servletRequestThreadLocal.get ();
	}

	@Override
	public
	HttpServletResponse response () {
		return servletResponseThreadLocal.get ();
	}

	@Override
	public
	ServletContext context () {
		return servletContextThreadLocal.get ();
	}

}
