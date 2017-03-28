package wbs.sms.number.list.fixture;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;

import wbs.sms.number.format.model.NumberFormatObjectHelper;
import wbs.sms.number.list.model.NumberListObjectHelper;

@PrototypeComponent ("numberListFixtureProvider")
public
class NumberListFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

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
	SliceObjectHelper sliceHelper;

	// implementation

	@Override
	public
	void createFixtures (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"createFixtures");

		createMenuItems (
			taskLogger);

		createNumberLists(
			taskLogger);

	}

	private
	void createMenuItems (
			@NonNull TaskLogger parentTaskLogger) {

		menuItemHelper.insert (
			menuItemHelper.createInstance ()

			.setMenuGroup (
				menuGroupHelper.findByCodeRequired (
					GlobalId.root,
					"test",
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

	}

	private
	void createNumberLists (
			@NonNull TaskLogger parentTaskLogger) {

		numberListHelper.insert (
			numberListHelper.createInstance ()

			.setSlice (
				sliceHelper.findByCodeRequired (
					GlobalId.root,
					"test"))

			.setCode (
				"uk_blocked")

			.setName (
				"UK blocked")

			.setDescription (
				"UK blocked (test)")

			.setNumberFormat (
				numberFormatHelper.findByCodeRequired (
					GlobalId.root,
					"uk"))

		);

	}

}
