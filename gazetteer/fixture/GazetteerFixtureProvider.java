package wbs.sms.gazetteer.fixture;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.record.GlobalId;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuGroupRec;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.menu.model.MenuItemRec;

@PrototypeComponent ("gazetteerFixtureProvider")
public
class GazetteerFixtureProvider
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

		MenuGroupRec smsMenuGroup =
			menuGroupHelper.findByCode (
				GlobalId.root,
				"test",
				"sms");

		menuItemHelper.insert (
			new MenuItemRec ()

			.setMenuGroup (
				smsMenuGroup)

			.setCode (
				"gazetteer")

			.setName (
				"Gazetteer")

			.setDescription (
				"Gazetteer")

			.setLabel (
				"Gazetteer")

			.setTargetPath (
				"/gazetteers")

			.setTargetFrame (
				"main")

		);

	}

}
