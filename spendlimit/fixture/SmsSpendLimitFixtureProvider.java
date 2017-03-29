package wbs.sms.spendlimit.fixture;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.logging.LogContext;
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
	implements FixtureProvider {

	// singleton dependencies

	@SingletonDependency
	CurrencyObjectHelper currencyHelper;

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

	// public implementation

	@Override
	public
	void createFixtures (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"createFixtures");

		createMenuItems (
			taskLogger);

		createFeatures (
			taskLogger);

		createSpendLimiter (
			taskLogger);

	}

	// private implementation

	private
	void createMenuItems (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"createMenuItems");

		menuItemHelper.insert (
			taskLogger,
			menuItemHelper.createInstance ()

			.setMenuGroup (
				menuGroupHelper.findByCodeRequired (
					GlobalId.root,
					"test",
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

	}

	private
	void createFeatures (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"createFeatures");

		featureHelper.insert (
			taskLogger,
			featureHelper.createInstance ()

			.setCode (
				"sms_spend_limit")

			.setName (
				"SMS spend limit")

			.setDescription (
				"Apply daily and ongoing SMS spend limits and advices")

		);

	}

	private
	void createSpendLimiter (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"createSpendLimiter");

		smsSpendLimiterHelper.insert (
			taskLogger,
			smsSpendLimiterHelper.createInstance ()

			.setSlice (
				sliceHelper.findByCodeRequired (
					GlobalId.root,
					"test"))

			.setCode (
				"test_sms_spend_limiter")

			.setName (
				"Test SMS spend limiter")

			.setDescription (
				"")

			.setCurrency (
				currencyHelper.findByCodeRequired (
					GlobalId.root,
					"test",
					"gbp"))

			.setRouter (
				routerHelper.findByCodeRequired (
					routeHelper.findByCodeRequired (
						GlobalId.root,
						"test",
						"free"),
					"static"))

			.setDailySpendLimitAmount (
				1000l)

			.setDailySpendLimitMessage (
				textHelper.findOrCreate (
					taskLogger,
					"Daily spend limit"))

			.setDailySpendAdviceAmount (
				500l)

			.setDailySpendAdviceMessage (
				textHelper.findOrCreate (
					taskLogger,
					"Daily spend advice"))

		);

	}

}
