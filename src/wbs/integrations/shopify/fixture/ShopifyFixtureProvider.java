package wbs.integrations.shopify.fixture;

import static wbs.utils.collection.MapUtils.mapItemForKeyRequired;
import static wbs.utils.collection.SetUtils.emptySet;
import static wbs.utils.string.CodeUtils.simplifyToCodeRequired;
import static wbs.utils.time.TimeUtils.isoTimestampString;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
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

import wbs.integrations.shopify.model.ShopifyAccountObjectHelper;

import wbs.platform.event.logic.EventFixtureLogic;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.platform.scaffold.model.SliceRec;

public
class ShopifyFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@SingletonDependency
	private
	Database database;

	@SingletonDependency
	private
	EventFixtureLogic eventFixtureLogic;

	@ClassSingletonDependency
	private
	LogContext logContext;

	@SingletonDependency
	private
	MenuGroupObjectHelper menuGroupHelper;

	@SingletonDependency
	private
	MenuItemObjectHelper menuItemHelper;

	@SingletonDependency
	private
	ShopifyAccountObjectHelper shopifyAccountHelper;

	@SingletonDependency
	private
	SliceObjectHelper sliceHelper;

	@SingletonDependency
	private
	TestAccounts testAccounts;

	@SingletonDependency
	private
	WbsConfig wbsConfig;

	// public implementation

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

			createStores (
				taskLogger);

		}

	}

	// private implementation

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
						"integration"))

				.setCode (
					"shopify")

				.setName (
					"Shopify")

				.setDescription (
					"Shopify")

				.setLabel (
					"Shopify")

				.setTargetPath (
					"/shopifyAccounts")

				.setTargetFrame (
					"main")

			);

			transaction.commit ();

		}

	}

	private
	void createStores (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"createStores");

		) {

			testAccounts.forEach (
				"shopify-account",
				suppliedParams -> {

				SliceRec slice =
					sliceHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						mapItemForKeyRequired (
							suppliedParams,
							"slice"));

				Map <String, String> allParams =
					ImmutableMap.<String, String> builder ()

					.putAll (
						suppliedParams)

					.put (
						"code",
						simplifyToCodeRequired (
							mapItemForKeyRequired (
								suppliedParams,
								"name")))

					.put (
						"nextFullSynchronise",
						isoTimestampString (
							transaction.now ()))

					.build ()

				;

				eventFixtureLogic.createRecordAndEvents (
					transaction,
					"Shopify",
					shopifyAccountHelper,
					slice,
					allParams,
					emptySet ());

			});

			transaction.commit ();

		}

	}

}
