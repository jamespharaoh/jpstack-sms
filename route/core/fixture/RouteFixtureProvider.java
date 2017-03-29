package wbs.sms.route.core.fixture;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

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

	// singleton dependencies

	@SingletonDependency
	CommandObjectHelper commandHelper;

	@SingletonDependency
	CurrencyObjectHelper currencyHelper;

	@SingletonDependency
	KeywordSetObjectHelper keywordSetHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MenuGroupObjectHelper menuGroupHelper;

	@SingletonDependency
	MenuItemObjectHelper menuItemHelper;

	@SingletonDependency
	RouteObjectHelper routeHelper;

	@SingletonDependency
	SliceObjectHelper sliceHelper;

	// implementation

	@Override
	public
	void createFixtures (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"createFixtures");

		routeHelper.insert (
			taskLogger,
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
			taskLogger,
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
			taskLogger,
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
			taskLogger,
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
