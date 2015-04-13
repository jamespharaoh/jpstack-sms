package wbs.sms.network.fixture;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.record.GlobalId;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.menu.model.MenuItemRec;
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
	MenuItemObjectHelper menuItemHelper;

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

		menuItemHelper.insert (
			new MenuItemRec ()

			.setMenuGroup (
				menuGroupHelper.findByCode (
					GlobalId.root,
					"test",
					"sms"))

			.setCode (
				"network")

			.setName (
				"Network")

			.setDescription (
				"Manage telephony network providers")

			.setLabel (
				"Network")

			.setTargetPath (
				"/networks")

			.setTargetFrame (
				"main")

		);

	}

}
