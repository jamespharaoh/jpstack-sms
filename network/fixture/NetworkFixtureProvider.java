package wbs.sms.network.fixture;

import javax.inject.Inject;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.sms.network.model.NetworkObjectHelper;

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
			networkHelper.createInstance ()

			.setId (
				0l)

			.setCode (
				"unknown")

			.setName (
				"Unknown")

			.setDescription (
				"Unknown")

		);

		networkHelper.insert (
			networkHelper.createInstance ()

			.setId (
				1l)

			.setCode (
				"blue")

			.setName (
				"Blue")

			.setDescription (
				"Blue")

		);

		networkHelper.insert (
			networkHelper.createInstance ()

			.setId (
				2l)

			.setCode (
				"red")

			.setName (
				"Red")

			.setDescription (
				"Red")

		);

		menuItemHelper.insert (
			menuItemHelper.createInstance ()

			.setMenuGroup (
				menuGroupHelper.findByCodeRequired (
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
