package wbs.platform.scaffold.fixture;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.scaffold.model.RootObjectHelper;

import wbs.utils.random.RandomLogic;

@PrototypeComponent ("rootFixtureProvider")
public
class RootFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	RandomLogic randomLogic;

	@SingletonDependency
	RootObjectHelper rootHelper;

	// implementation

	@Override
	public
	void createFixtures (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"createFixtures");

		) {

			rootHelper.insert (
				transaction,
				rootHelper.createInstance ()

				.setId (
					0l)

				.setCode (
					"root")

				.setFixturesSeed (
					randomLogic.generateLowercase (
						20))

				.setInstallationId (
					randomLogic.randomInteger ())

			);

			transaction.commit ();

		}

	}

}
