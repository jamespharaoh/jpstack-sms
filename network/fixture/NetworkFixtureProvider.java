package wbs.sms.network.fixture;

import static wbs.utils.etc.OptionalUtils.optionalFromNullable;
import static wbs.utils.etc.OptionalUtils.optionalMapRequired;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.string.CodeUtils.simplifyToCodeRequired;

import java.util.List;

import com.google.common.collect.ImmutableList;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.Fixture;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;

import wbs.sms.network.model.NetworkObjectHelper;

@PrototypeComponent ("networkFixtureProvider")
public
class NetworkFixtureProvider
	implements Fixture {

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
	NetworkObjectHelper networkHelper;

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

			createMenuItems (
				taskLogger);

			createDefaultNetworks (
				taskLogger);

		}

	}

	private
	void createMenuItems (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"createMenuItems");

		) {

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

			transaction.commit ();

		}

	}

	private
	void createDefaultNetworks (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"createDefaultNetworks");

		) {

			defaultNetworks.forEach (
				defaultNetwork ->
					networkHelper.insert (
						transaction,
						networkHelper.createInstance ()

				.setId (
					defaultNetwork.id ())

				.setCode (
					simplifyToCodeRequired (
						defaultNetwork.name ()))

				.setName (
					defaultNetwork.name ())

				.setDescription (
					defaultNetwork.description ())

				.setVirtualNetworkOfNetwork (
					optionalOrNull (
						optionalMapRequired (
							optionalFromNullable (
								defaultNetwork.virtualNetworkOf ()),
							virtualNetworkOf ->
								networkHelper.findRequired (
									transaction,
									virtualNetworkOf))))

			));

			transaction.commit ();

		}

	}

	// types

	@Accessors (fluent = true)
	@Data
	public static
	class DefaultNetwork {
		Long id;
		String name;
		String description;
		Long virtualNetworkOf;
	}

	// default data

	List <DefaultNetwork> defaultNetworks =
		ImmutableList.of (

		new DefaultNetwork ()
			.id (0l)
			.name ("Unknown")
			.description ("Unknown"),

		new DefaultNetwork ()
			.id (1l)
			.name ("UK Orange")
			.description ("Orange UK"),

		new DefaultNetwork ()
			.id (2l)
			.name ("UK Vodafone")
			.description ("Vodafone UK"),

		new DefaultNetwork ()
			.id (3l)
			.name ("UK TMobile")
			.description ("T-Mobile UK"),

		new DefaultNetwork ()
			.id (4l)
			.name ("UK O2")
			.description ("O2 UK"),

		new DefaultNetwork ()
			.id (5l)
			.name ("UK Virgin")
			.description ("Virgin UK")
			.virtualNetworkOf (3l),

		new DefaultNetwork ()
			.id (6l)
			.name ("UK Three")
			.description ("Three UK")

	);

}
