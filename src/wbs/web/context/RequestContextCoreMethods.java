package wbs.web.context;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public
interface RequestContextCoreMethods {

	ServletContext context ();
	HttpServletRequest request ();
	HttpServletResponse response ();

}
