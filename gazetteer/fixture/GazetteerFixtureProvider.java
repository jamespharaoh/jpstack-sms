package wbs.sms.gazetteer.fixture;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.record.GlobalId;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuGroupRec;
import wbs.platform.menu.model.MenuObjectHelper;
import wbs.platform.menu.model.MenuRec;

@PrototypeComponent ("gazetteerFixtureProvider")
public
class GazetteerFixtureProvider
	implements FixtureProvider {

	// dependencies

	@Inject
	MenuGroupObjectHelper menuGroupHelper;

	@Inject
	MenuObjectHelper menuHelper;

	// implementation

	@Override
	public
	void createFixtures () {

		MenuGroupRec smsMenuGroup =
			menuGroupHelper.findByCode (
				GlobalId.root,
				"sms");

		menuHelper.insert (
			new MenuRec ()

			.setMenuGroup (
				smsMenuGroup)

			.setCode (
				"gazetteer")

			.setLabel (
				"Gazetteer")

			.setPath (
				"/gazetteers")

		);

	}

}
