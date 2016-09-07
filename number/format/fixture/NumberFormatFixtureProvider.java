package wbs.sms.number.format.fixture;

import javax.inject.Inject;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.sms.number.format.model.NumberFormatObjectHelper;
import wbs.sms.number.format.model.NumberFormatPatternObjectHelper;

@PrototypeComponent ("numberFormatFixtureProvider")
public
class NumberFormatFixtureProvider
	implements FixtureProvider {

	// dependencies

	@Inject
	Database database;

	@Inject
	MenuGroupObjectHelper menuGroupHelper;

	@Inject
	MenuItemObjectHelper menuItemHelper;

	@Inject
	NumberFormatObjectHelper numberFormatHelper;

	@Inject
	NumberFormatPatternObjectHelper numberFormatPatternHelper;

	// implementation

	@Override
	public
	void createFixtures () {

		numberFormatHelper.insert (
			numberFormatHelper.createInstance ()

			.setCode (
				"uk")

			.setName (
				"UK")

			.setDescription (
				"United Kingdom")

		);

		database.flush ();

		numberFormatPatternHelper.insert (
			numberFormatPatternHelper.createInstance ()

			.setNumberFormat (
				numberFormatHelper.findByCodeRequired (
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
			numberFormatPatternHelper.createInstance ()

			.setNumberFormat (
				numberFormatHelper.findByCodeRequired (
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
			numberFormatPatternHelper.createInstance ()

			.setNumberFormat (
				numberFormatHelper.findByCodeRequired (
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
			menuItemHelper.createInstance ()

			.setMenuGroup (
				menuGroupHelper.findByCodeRequired (
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
