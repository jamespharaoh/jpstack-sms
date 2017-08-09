package wbs.framework.fixtures;

import wbs.framework.logging.TaskLogger;

public
interface FixtureProvider {

	void createFixtures (
			TaskLogger parentTaskLogger);

}
