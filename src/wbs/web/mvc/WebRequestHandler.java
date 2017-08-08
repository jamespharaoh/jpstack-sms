package wbs.web.mvc;

import wbs.framework.logging.TaskLogger;

/**
 * A RequestHandler simply handles a request.
 *
 * These are delegated to by SimpleWebFile depending on the method.
 */
public
interface WebRequestHandler {

	public
	void handle (
			TaskLogger parentTaskLogger);

}
