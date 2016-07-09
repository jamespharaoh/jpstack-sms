package wbs.sms.route.core.fixture;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.record.GlobalId;
import wbs.platform.currency.model.CurrencyObjectHelper;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.sms.command.model.CommandObjectHelper;
import wbs.sms.keyword.model.KeywordSetObjectHelper;
import wbs.sms.route.core.model.RouteObjectHelper;

@PrototypeComponent ("routeFixtureProvider")
public
class RouteFixtureProvider
	implements FixtureProvider {

	// dependencies

	@Inject
	CommandObjectHelper commandHelper;

	@Inject
	CurrencyObjectHelper currencyHelper;

	@Inject
	KeywordSetObjectHelper keywordSetHelper;

	@Inject
	MenuGroupObjectHelper menuGroupHelper;

	@Inject
	MenuItemObjectHelper menuItemHelper;

	@Inject
	RouteObjectHelper routeHelper;

	@Inject
	SliceObjectHelper sliceHelper;

	// implementation

	@Override
	public
	void createFixtures () {

		routeHelper.insert (
			routeHelper.createInstance ()

			.setSlice (
				sliceHelper.findByCodeRequired (
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
				commandHelper.findByCodeRequired (
					keywordSetHelper.findByCodeRequired (
						GlobalId.root,
						"test",
						"inbound"),
					"default"))

		);

		routeHelper.insert (
			routeHelper.createInstance ()

			.setSlice (
				sliceHelper.findByCodeRequired (
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
			routeHelper.createInstance ()

			.setSlice (
				sliceHelper.findByCodeRequired (
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
				500l)

			.setCurrency (
				currencyHelper.findByCodeRequired (
					GlobalId.root,
					"test",
					"gbp"))

		);

		menuItemHelper.insert (
			menuItemHelper.createInstance ()

			.setMenuGroup (
				menuGroupHelper.findByCodeRequired (
					GlobalId.root,
					"test",
					"sms"))

			.setCode (
				"route")

			.setName (
				"Route")

			.setDescription (
				"")

			.setLabel (
				"Routes")

			.setTargetPath (
				"/routes")

			.setTargetFrame (
				"main")

		);

	}

}
