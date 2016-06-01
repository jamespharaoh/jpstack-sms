package wbs.sms.number.list.fixture;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.record.GlobalId;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;

@PrototypeComponent ("numberListFixtureProvider")
public
class NumberListFixtureProvider
	implements FixtureProvider {

	// dependencies

	@Inject
	MenuGroupObjectHelper menuGroupHelper;

	@Inject
	MenuItemObjectHelper menuItemHelper;

	// implementation

	@Override
	public
	void createFixtures () {

		menuItemHelper.insert (
			menuItemHelper.createInstance ()

			.setMenuGroup (
				menuGroupHelper.findByCodeOrNull (
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

}
