package wbs.sms.magicnumber.fixture;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.record.GlobalId;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;

@PrototypeComponent ("magicNumberFixtureProvider")
public
class MagicNumberFixtureProvider
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
				"magic_number")

			.setName (
				"Magic Number")

			.setDescription (
				"Magic number")

			.setLabel (
				"Magic number")

			.setTargetPath (
				"/magicNumberSets")

			.setTargetFrame (
				"main")

		);

	}

}
