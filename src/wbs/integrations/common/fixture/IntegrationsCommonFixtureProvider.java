package wbs.integrations.common.fixture;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;

@PrototypeComponent ("integrationsCommonFixtureProvider")
public
class IntegrationsCommonFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	private
	LogContext logContext;

	@SingletonDependency
	private
	MenuGroupObjectHelper menuGroupHelper;

	@SingletonDependency
	private
	SliceObjectHelper sliceHelper;

	@SingletonDependency
	private
	WbsConfig wbsConfig;

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

			menuGroupHelper.insert (
				transaction,
				menuGroupHelper.createInstance ()

				.setSlice (
					sliceHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						wbsConfig.defaultSlice ()))

				.setCode (
					"integration")

				.setName (
					"Integration")

				.setDescription (
					"")

				.setLabel (
					"Integrations")

				.setOrder (
					60l)

			);

			transaction.commit ();

		}

	}

}
