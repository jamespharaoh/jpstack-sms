package wbs.sms.route.core.fixture;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.Fixture;
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
	implements Fixture {

	// singleton dependencies

	@SingletonDependency
	CommandObjectHelper commandHelper;

	@SingletonDependency
	CurrencyObjectHelper currencyHelper;

	@SingletonDependency
	Database database;

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

	@SingletonDependency
	WbsConfig wbsConfig;

	// implementation

	@Override
	public
	void createFixtures (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"createFixtures");

		) {

			routeHelper.insert (
				transaction,
				routeHelper.createInstance ()

				.setSlice (
					sliceHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						wbsConfig.defaultSlice ()))

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
						transaction,
						keywordSetHelper.findByCodeRequired (
							transaction,
							GlobalId.root,
							wbsConfig.defaultSlice (),
							"inbound"),
						"default"))

			);

			routeHelper.insert (
				transaction,
				routeHelper.createInstance ()

				.setSlice (
					sliceHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						wbsConfig.defaultSlice ()))

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
				transaction,
				routeHelper.createInstance ()

				.setSlice (
					sliceHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						wbsConfig.defaultSlice ()))

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
						transaction,
						GlobalId.root,
						wbsConfig.defaultSlice (),
						"gbp"))

			);

			menuItemHelper.insert (
				transaction,
				menuItemHelper.createInstance ()

				.setMenuGroup (
					menuGroupHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						wbsConfig.defaultSlice (),
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

			transaction.commit ();

		}

	}

}
