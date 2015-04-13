package wbs.sms.number.core.fixture;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.record.GlobalId;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.menu.model.MenuItemRec;

@PrototypeComponent ("numberFixtureProvider")
public
class NumberFixtureProvider
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
			new MenuItemRec ()

			.setMenuGroup (
				menuGroupHelper.findByCode (
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
