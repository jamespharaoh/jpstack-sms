package wbs.test.simulator.fixture;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.platform.user.model.UserObjectHelper;

import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.core.model.RouteRec;
import wbs.sms.route.sender.model.SenderObjectHelper;

import wbs.test.simulator.model.SimulatorObjectHelper;
import wbs.test.simulator.model.SimulatorRec;
import wbs.test.simulator.model.SimulatorRouteObjectHelper;
import wbs.test.simulator.model.SimulatorSessionObjectHelper;

@PrototypeComponent ("simulatorFixtureProvider")
public
class SimulatorFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MenuGroupObjectHelper menuGroupHelper;

	@SingletonDependency
	MenuItemObjectHelper menuItemHelper;

	@SingletonDependency
	RouteObjectHelper routeHelper;

	@SingletonDependency
	SenderObjectHelper senderHelper;

	@SingletonDependency
	SimulatorObjectHelper simulatorHelper;

	@SingletonDependency
	SimulatorRouteObjectHelper simulatorRouteHelper;

	@SingletonDependency
	SimulatorSessionObjectHelper simulatorSessionHelper;

	@SingletonDependency
	SliceObjectHelper sliceHelper;

	@SingletonDependency
	UserObjectHelper userHelper;

	@SingletonDependency
	WbsConfig wbsConfig;

	// implementation

	@Override
	public
	void createFixtures (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"createFixtures");

		) {

			createMenuItem (
				taskLogger);

			createSimulator (
				taskLogger);

		}

	}

	private
	void createMenuItem (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"createMenuItem");

		) {

			menuItemHelper.insert (
				transaction,
				menuItemHelper.createInstance ()

				.setMenuGroup (
					menuGroupHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						wbsConfig.defaultSlice (),
						"test"))

				.setCode (
					"simulator")

				.setName (
					"Simulator")

				.setDescription (
					"")

				.setLabel (
					"Simulator")

				.setTargetPath (
					"/simulators")

				.setTargetFrame (
					"main")

			);

			transaction.commit ();

		}

	}

	private
	void createSimulator (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"createSimulator");

		) {

			SimulatorRec simulator =
				simulatorHelper.insert (
					transaction,
					simulatorHelper.createInstance ()

				.setSlice (
					sliceHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						wbsConfig.defaultSlice ()))

				.setCode (
					"test")

				.setName (
					"Test")

				.setDescription (
					"Test")

			);

			// free route

			RouteRec freeRoute =
				routeHelper.findByCodeRequired (
					transaction,
					GlobalId.root,
					wbsConfig.defaultSlice (),
					"free");

			freeRoute

				.setSender (
					senderHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						"simulator"));

			// bill route

			RouteRec billRoute =
				routeHelper.findByCodeRequired (
					transaction,
					GlobalId.root,
					wbsConfig.defaultSlice (),
					"bill");

			billRoute

				.setSender (
					senderHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						"simulator"));

			// magic route

			/*
			RouteRec magicRoute =
				routeHelper.findByCode (
					GlobalId.root,
					"test",
					"magic");

			simulatorRouteHelper.insert (
				new SimulatorRouteRec ()

				.setSimulator (
					simulator)

				.setPrefix (
					"magic")

				.setDescription (
					"Magic")

				.setRoute (
					magicRoute)

			);
			*/

			// inbound route

			RouteRec inboundRoute =
				routeHelper.findByCodeRequired (
					transaction,
					GlobalId.root,
					wbsConfig.defaultSlice (),
					"inbound");

			simulatorRouteHelper.insert (
				transaction,
				simulatorRouteHelper.createInstance ()

				.setSimulator (
					simulator)

				.setPrefix (
					"inbound")

				.setDescription (
					"Inbound")

				.setRoute (
					inboundRoute)

			);

			// session

			simulatorSessionHelper.insert (
				transaction,
				simulatorSessionHelper.createInstance ()

				.setSimulator (
					simulator)

				.setDescription (
					"Test simulator session")

				.setCreatedTime (
					transaction.now ())

				.setCreatedUser (
					userHelper.findRequired (
						transaction,
						1l))

			);

			transaction.commit ();

		}

	}

}
