package wbs.sms.number.format.fixture;

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

import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;

import wbs.sms.number.format.model.NumberFormatObjectHelper;
import wbs.sms.number.format.model.NumberFormatPatternObjectHelper;

@PrototypeComponent ("numberFormatFixtureProvider")
public
class NumberFormatFixtureProvider
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
	NumberFormatObjectHelper numberFormatHelper;

	@SingletonDependency
	NumberFormatPatternObjectHelper numberFormatPatternHelper;

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

			numberFormatHelper.insert (
				transaction,
				numberFormatHelper.createInstance ()

				.setCode (
					"uk")

				.setName (
					"UK")

				.setDescription (
					"United Kingdom")

			);

			transaction.flush ();

			numberFormatPatternHelper.insert (
				transaction,
				numberFormatPatternHelper.createInstance ()

				.setNumberFormat (
					numberFormatHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						"uk"))

				.setInputPrefix (
					"44")

				.setMinimumLength (
					12l)

				.setMaximumLength (
					12l)

				.setOutputPrefix (
					"44")

			);

			numberFormatPatternHelper.insert (
				transaction,
				numberFormatPatternHelper.createInstance ()

				.setNumberFormat (
					numberFormatHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						"uk"))

				.setInputPrefix (
					"+44")

				.setMinimumLength (
					13l)

				.setMaximumLength (
					13l)

				.setOutputPrefix (
					"44")

			);

			numberFormatPatternHelper.insert (
				transaction,
				numberFormatPatternHelper.createInstance ()

				.setNumberFormat (
					numberFormatHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						"uk"))

				.setInputPrefix (
					"0")

				.setMinimumLength (
					11l)

				.setMaximumLength (
					11l)

				.setOutputPrefix (
					"44")

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
					"number_format")

				.setName (
					"Number Format")

				.setDescription (
					"Manage localized telephony number conventions")

				.setLabel (
					"Number format")

				.setTargetPath (
					"/numberFormats")

				.setTargetFrame (
					"main")

			);

			transaction.commit ();

		}

	}

}
