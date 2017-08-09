package wbs.platform.user.fixture;

import static wbs.utils.collection.MapUtils.mapItemForKeyRequired;
import static wbs.utils.string.StringUtils.stringSplitComma;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.fixtures.TestAccounts;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.platform.event.logic.EventFixtureLogic;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.priv.model.PrivObjectHelper;
import wbs.platform.priv.model.PrivRec;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.platform.scaffold.model.SliceRec;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserPrivObjectHelper;
import wbs.platform.user.model.UserRec;

@PrototypeComponent ("userFixtureProvider")
public
class UserFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	EventFixtureLogic eventFixtureLogic;

	@SingletonDependency
	EventLogic eventLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MenuGroupObjectHelper menuGroupHelper;

	@SingletonDependency
	MenuItemObjectHelper menuItemHelper;

	@SingletonDependency
	PrivObjectHelper privHelper;

	@SingletonDependency
	SliceObjectHelper sliceHelper;

	@SingletonDependency
	TestAccounts testAccounts;

	@SingletonDependency
	UserObjectHelper userHelper;

	@SingletonDependency
	UserPrivObjectHelper userPrivHelper;

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

			createUsers (
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
						"system"))

				.setCode (
					"user")

				.setName (
					"User")

				.setDescription (
					"")

				.setLabel (
					"Users")

				.setTargetPath (
					"/users")

				.setTargetFrame (
					"main")

			);

			transaction.commit ();

		}

	}

	private
	void createUsers (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"createUsers");

		) {

			Set <String> ignoreParams =
				ImmutableSet.of (
					"slice",
					"password-hash",
					"privs");

			testAccounts.forEach (
				"user",
				suppliedParams -> {

				Map <String, String> allParams =
					ImmutableMap.<String, String> builder ()

					.putAll (
						suppliedParams)

					.put (
						"active",
						"yes")

					.build ()

				;

				SliceRec slice =
					sliceHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						mapItemForKeyRequired (
							suppliedParams,
							"slice"));

				UserRec user =
					eventFixtureLogic.createRecordAndEvents (
						transaction,
						"Deployment",
						userHelper,
						slice,
						allParams,
						ignoreParams);

				user

					.setPassword (
						mapItemForKeyRequired (
							suppliedParams,
							"password-hash"))

				;

				eventLogic.createEvent (
					transaction,
					"user_password_set_by_fixture",
					"User",
					user);

				for (
					String privCode
						: stringSplitComma (
							mapItemForKeyRequired (
								suppliedParams,
								"privs"))
				) {

					PrivRec priv =
						privHelper.findByCodeRequired (
							transaction,
							GlobalId.root,
							privCode);

					userPrivHelper.insert (
						transaction,
						userPrivHelper.createInstance ()

						.setUser (
							user)

						.setPriv (
							priv)

						.setCan (
							true)

					);

					eventLogic.createEvent (
						transaction,
						"user_grant_by_fixture",
						"User",
						user,
						priv);

				}

			});

			transaction.commit ();

		}

	}

}
