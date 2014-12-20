package wbs.sms.route.core.fixture;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.record.GlobalId;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuObjectHelper;
import wbs.platform.menu.model.MenuRec;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.keyword.model.KeywordSetObjectHelper;
import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;

@PrototypeComponent ("routeFixtureProvider")
public
class RouteFixtureProvider
	implements FixtureProvider {

	// dependencies

	@Inject
	CommandObjectHelper commandHelper;

	@Inject
	KeywordSetObjectHelper keywordSetHelper;

	@Inject
	MenuGroupObjectHelper menuGroupHelper;

	@Inject
	MenuObjectHelper menuHelper;

	@Inject
	RouteObjectHelper routeHelper;

	@Inject
	SliceObjectHelper sliceHelper;

	// implementation

	@Override
	public
	void createFixtures () {

		routeHelper.insert (
			new RouteRec ()

			.setSlice (
				sliceHelper.findByCode (
					GlobalId.root,
					"test"))

			.setCode (
				"inbound")

			.setName (
				"Inbound")

			.setDescription (
				"Inbound")

			.setNumber (
				"in")

			.setCanReceive (
				true)

			.setCommand (
				commandHelper.findByCode (
					keywordSetHelper.findByCode (
						sliceHelper.findByCode (
							GlobalId.root,
							"test"),
						"inbound"),
					"default"))

		);

		routeHelper.insert (
			new RouteRec ()

			.setSlice (
				sliceHelper.findByCode (
					GlobalId.root,
					"test"))

			.setCode (
				"free")

			.setName (
				"Free")

			.setDescription (
				"Free")

			.setCanSend (
				true)

			.setDeliveryReports (
				true)

		);

		routeHelper.insert (
			new RouteRec ()

			.setSlice (
				sliceHelper.findByCode (
					GlobalId.root,
					"test"))

			.setCode (
				"bill")

			.setName (
				"Bill")

			.setDescription (
				"Bill")

			.setCanSend (
				true)

			.setDeliveryReports (
				true)

			.setOutCharge (
				500)

		);

		menuHelper.insert (
			new MenuRec ()

			.setMenuGroup (
				menuGroupHelper.findByCode (
					GlobalId.root,
					"sms"))

			.setCode (
				"route")

			.setLabel (
				"Routes")

			.setPath (
				"/routes")

		);

	}

}
