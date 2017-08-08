package wbs.web.mvc;

import wbs.framework.logging.TaskLogger;

public
interface WebExceptionHandler {

	void handleExceptionRetry (
			TaskLogger parentTaskLogger,
			Long attempt,
			Throwable exception);

	void handleExceptionFinal (
			TaskLogger parentTaskLogger,
			Long attempt,
			Throwable exception);

}
