package wbs.web.responder;

import wbs.framework.logging.TaskLogger;

public
interface WebResponder {

	public
	void execute (
			TaskLogger parentTaskLogger);

}
