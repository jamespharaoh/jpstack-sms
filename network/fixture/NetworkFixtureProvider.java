package wbs.sms.network.fixture;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.record.GlobalId;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuObjectHelper;
import wbs.platform.menu.model.MenuRec;
import wbs.sms.network.model.NetworkObjectHelper;
import wbs.sms.network.model.NetworkRec;

@PrototypeComponent ("networkFixtureProvider")
public
class NetworkFixtureProvider
	implements FixtureProvider {

	// dependencies

	@Inject
	MenuGroupObjectHelper menuGroupHelper;

	@Inject
	MenuObjectHelper menuHelper;

	@Inject
	NetworkObjectHelper networkHelper;

	// implementation

	@Override
	public
	void createFixtures () {

		networkHelper.insert (
			new NetworkRec ()

			.setId (
				0)

			.setCode (
				"unknown")

			.setName (
				"Unknown")

			.setDescription (
				"Unknown")

		);

		networkHelper.insert (
			new NetworkRec ()

			.setId (
				1)

			.setCode (
				"blue")

			.setName (
				"Blue")

			.setDescription (
				"Blue")

		);

		networkHelper.insert (
			new NetworkRec ()

			.setId (
				2)

			.setCode (
				"red")

			.setName (
				"Red")

			.setDescription (
				"Red")

		);

		menuHelper.insert (
			new MenuRec ()

			.setMenuGroup (
				menuGroupHelper.findByCode (
					GlobalId.root,
					"sms"))

			.setCode (
				"network")

			.setLabel (
				"Networks")

			.setPath (
				"/networks")

		);

	}

}
