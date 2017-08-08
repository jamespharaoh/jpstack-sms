package wbs.web.file;

import wbs.framework.logging.TaskLogger;

/**
 * Abstraction to handle GET/POST requests for a single "file".
 *
 * The same WebFile can easily be reused for any number of different paths
 * etc... so the name here might be slightly misleading.
 */
public
interface WebFile {

	void doGet (
			TaskLogger taskLogger);

	void doPost (
			TaskLogger taskLogger);

	void doOptions (
			TaskLogger taskLogger);

}
