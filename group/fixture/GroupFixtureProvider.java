package wbs.platform.group.fixture;

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

import wbs.platform.group.model.GroupObjectHelper;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;

@PrototypeComponent ("groupFixtureProvider")
public
class GroupFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	GroupObjectHelper groupHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MenuGroupObjectHelper menuGroupHelper;

	@SingletonDependency
	MenuItemObjectHelper menuItemHelper;

	@SingletonDependency
	SliceObjectHelper sliceHelper;

	@SingletonDependency
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

			// menu item

			menuItemHelper.insert (
				transaction,
				menuItemHelper.createInstance ()

				.setMenuGroup (
					menuGroupHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						wbsConfig.defaultSlice (),
						"system"))

				.setCode (
					"group")

				.setName (
					"Group")

				.setDescription (
					"Group")

				.setLabel (
					"Groups")

				.setTargetPath (
					"/groups")

				.setTargetFrame (
					"main")

			);

			// test groups

			for (
				int index = 0;
				index < 10;
				index ++
			) {

				groupHelper.insert (
					transaction,
					groupHelper.createInstance ()

					.setSlice (
						sliceHelper.findByCodeRequired (
							transaction,
							GlobalId.root,
							wbsConfig.defaultSlice ()))

					.setCode (
						"test_" + index)

					.setName (
						"Test " + index)

					.setDescription (
						"")

				);

			}

			transaction.commit ();

		}

	}

}
