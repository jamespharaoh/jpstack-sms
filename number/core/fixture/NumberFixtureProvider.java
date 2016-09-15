package wbs.sms.number.core.fixture;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;

@PrototypeComponent ("numberFixtureProvider")
public
class NumberFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@SingletonDependency
	MenuGroupObjectHelper menuGroupHelper;

	@SingletonDependency
	MenuItemObjectHelper menuItemHelper;

	// implementation

	@Override
	public
	void createFixtures () {

		menuItemHelper.insert (
			menuItemHelper.createInstance ()

			.setMenuGroup (
				menuGroupHelper.findByCodeRequired (
					GlobalId.root,
					"test",
					"sms"))

			.setCode (
				"number")

			.setName (
				"Number")

			.setDescription (
				"Manage individual telephony subscriber numbers")

			.setLabel (
				"Numbers")

			.setTargetPath (
				"/numbers")

			.setTargetFrame (
				"main")

		);

	}


}
