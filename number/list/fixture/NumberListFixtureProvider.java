package wbs.sms.number.list.fixture;

import com.google.common.collect.ImmutableList;

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

import wbs.sms.number.core.model.NumberObjectHelper;
import wbs.sms.number.format.model.NumberFormatObjectHelper;
import wbs.sms.number.list.model.NumberListNumberObjectHelper;
import wbs.sms.number.list.model.NumberListObjectHelper;
import wbs.sms.number.list.model.NumberListRec;

@PrototypeComponent ("numberListFixtureProvider")
public
class NumberListFixtureProvider
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
	NumberFormatObjectHelper numberFormatHelper;

	@SingletonDependency
	NumberListObjectHelper numberListHelper;

	@SingletonDependency
	NumberListNumberObjectHelper numberListNumberHelper;

	@SingletonDependency
	NumberObjectHelper numberHelper;

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

			createMenuItems (
				taskLogger);

			createNumberLists(
				taskLogger);

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
						"sms"))

				.setCode (
					"number_list")

				.setName (
					"Number List")

				.setDescription (
					"Manage dynamic lists of telephone numbers")

				.setLabel (
					"Number list")

				.setTargetPath (
					"/numberLists")

				.setTargetFrame (
					"main")

			);

			transaction.commit ();

		}

	}

	private
	void createNumberLists (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"createNumberLists");

		) {

			NumberListRec numberList =
				numberListHelper.insert (
					transaction,
					numberListHelper.createInstance ()

				.setSlice (
					sliceHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						wbsConfig.defaultSlice ()))

				.setCode (
					"uk_blocked")

				.setName (
					"UK blocked")

				.setDescription (
					"UK blocked (test)")

				.setNumberFormat (
					numberFormatHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						"uk"))

			);

			for (
				String number
					: ImmutableList.of (
						"447000000000",
						"447111111111",
						"447222222222",
						"447333333333",
						"447444444444",
						"447555555555",
						"447666666666",
						"447777777777",
						"447888888888",
						"447999999999")
			) {

				numberListNumberHelper.insert (
					transaction,
					numberListNumberHelper.createInstance ()

					.setNumberList (
						numberList)

					.setNumber (
						numberHelper.findOrCreate (
							transaction,
							number))

					.setPresent (
						true)

				);

				numberList.setNumberCount (
					numberList.getNumberCount () + 1);

			}

			transaction.commit ();

		}

	}

}
