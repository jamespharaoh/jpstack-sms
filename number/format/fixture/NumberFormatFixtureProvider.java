package wbs.sms.number.format.fixture;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.logging.LogContext;

import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;

import wbs.sms.number.format.model.NumberFormatObjectHelper;
import wbs.sms.number.format.model.NumberFormatPatternObjectHelper;

@PrototypeComponent ("numberFormatFixtureProvider")
public
class NumberFormatFixtureProvider
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
	NumberFormatObjectHelper numberFormatHelper;

	@SingletonDependency
	NumberFormatPatternObjectHelper numberFormatPatternHelper;

	// implementation

	@Override
	public
	void createFixtures (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
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
						"test",
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

		}

	}

}
