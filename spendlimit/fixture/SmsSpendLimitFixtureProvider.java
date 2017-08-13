package wbs.sms.spendlimit.fixture;

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
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.platform.currency.model.CurrencyObjectHelper;
import wbs.platform.feature.model.FeatureObjectHelper;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.platform.text.model.TextObjectHelper;

import wbs.sms.route.core.model.RouteObjectHelper;
import wbs.sms.route.router.model.RouterObjectHelper;
import wbs.sms.spendlimit.model.SmsSpendLimiterObjectHelper;

@PrototypeComponent ("smsSpendLimitFixtureProvider")
public
class SmsSpendLimitFixtureProvider
	implements Fixture {

	// singleton dependencies

	@SingletonDependency
	CurrencyObjectHelper currencyHelper;

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	FeatureObjectHelper featureHelper;

	@SingletonDependency
	MenuGroupObjectHelper menuGroupHelper;

	@SingletonDependency
	MenuItemObjectHelper menuItemHelper;

	@SingletonDependency
	RouteObjectHelper routeHelper;

	@SingletonDependency
	RouterObjectHelper routerHelper;

	@SingletonDependency
	SliceObjectHelper sliceHelper;

	@SingletonDependency
	SmsSpendLimiterObjectHelper smsSpendLimiterHelper;

	@SingletonDependency
	TextObjectHelper textHelper;

	@SingletonDependency
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

			createFeatures (
				taskLogger);

			createSpendLimiter (
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
						"sms"))

				.setCode (
					"spend_limiter")

				.setName (
					"Spend limiter")

				.setDescription (
					"")

				.setLabel (
					"Spend limiters")

				.setTargetPath (
					"/smsSpendLimiters")

				.setTargetFrame (
					"main")

			);

			transaction.commit ();

		}

	}

	private
	void createFeatures (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"createFeatures");

		) {

			featureHelper.insert (
				transaction,
				featureHelper.createInstance ()

				.setCode (
					"sms_spend_limit")

				.setName (
					"SMS spend limit")

				.setDescription (
					"Apply daily and ongoing SMS spend limits and advices")

			);

			transaction.commit ();

		}

	}

	private
	void createSpendLimiter (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"createSpendLimiter");

		) {

			smsSpendLimiterHelper.insert (
				transaction,
				smsSpendLimiterHelper.createInstance ()

				.setSlice (
					sliceHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						wbsConfig.defaultSlice ()))

				.setCode (
					"test_sms_spend_limiter")

				.setName (
					"Test SMS spend limiter")

				.setDescription (
					"")

				.setCurrency (
					currencyHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						wbsConfig.defaultSlice (),
						"gbp"))

				.setRouter (
					routerHelper.findByCodeRequired (
						transaction,
						routeHelper.findByCodeRequired (
							transaction,
							GlobalId.root,
							wbsConfig.defaultSlice (),
							"free"),
						"static"))

				.setDailySpendLimitAmount (
					1000l)

				.setDailySpendLimitMessage (
					textHelper.findOrCreate (
						transaction,
						"Daily spend limit"))

				.setDailySpendAdviceAmount (
					500l)

				.setDailySpendAdviceMessage (
					textHelper.findOrCreate (
						transaction,
						"Daily spend advice"))

			);

			transaction.commit ();

		}

	}

}
