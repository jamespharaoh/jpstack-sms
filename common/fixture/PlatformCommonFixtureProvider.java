package wbs.platform.common.fixture;

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
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;

@PrototypeComponent ("platformCommonFixtureProvider")
public
class PlatformCommonFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@SingletonDependency
	Database database;

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

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"createFixtures");

		) {

			createMenuGroups (
				taskLogger);

			createMenuItems (
				taskLogger);

		}

	}

	private
	void createMenuGroups (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"createMenuGroups");

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
					"panel")

				.setName (
					"Panel")

				.setDescription (
					"")

				.setLabel (
					"Panels")

				.setOrder (
					20l)

			);

			menuGroupHelper.insert (
				transaction,
				menuGroupHelper.createInstance ()

				.setSlice (
					sliceHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						wbsConfig.defaultSlice ()))

				.setCode (
					"system")

				.setName (
					"System")

				.setDescription (
					"")

				.setLabel (
					"System")

				.setOrder (
					50l)

			);

			menuGroupHelper.insert (
				transaction,
				menuGroupHelper.createInstance ()

				.setSlice (
					sliceHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						wbsConfig.defaultSlice ()))

				.setCode (
					"internal")

				.setName (
					"Internal")

				.setDescription (
					"")

				.setLabel (
					"Internals")

				.setOrder (
					70l)

			);

			transaction.commit ();

		}

	}

	private
	void createMenuItems (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"createMenuItems");

		) {

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
					"slice")

				.setName (
					"Slice")

				.setDescription (
					"")

				.setLabel (
					"Slice")

				.setTargetPath (
					"/slices")

				.setTargetFrame (
					"main")

			);

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
					"menu")

				.setName (
					"Menu")

				.setDescription (
					"")

				.setLabel (
					"Menu")

				.setTargetPath (
					"/menuGroups")

				.setTargetFrame (
					"main")

			);

			transaction.commit ();

		}

	}

}
