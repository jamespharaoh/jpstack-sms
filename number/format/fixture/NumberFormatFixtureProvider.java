package wbs.sms.number.format.fixture;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.record.GlobalId;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.menu.model.MenuItemRec;
import wbs.sms.number.format.model.NumberFormatObjectHelper;
import wbs.sms.number.format.model.NumberFormatPatternObjectHelper;
import wbs.sms.number.format.model.NumberFormatPatternRec;
import wbs.sms.number.format.model.NumberFormatRec;

@PrototypeComponent ("numberFormatFixtureProvider")
public
class NumberFormatFixtureProvider
	implements FixtureProvider {

	// dependencies

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
			new NumberFormatRec ()

			.setCode (
				"uk")

			.setName (
				"UK")

			.setDescription (
				"United Kingdom")

		);

		numberFormatPatternHelper.insert (
			new NumberFormatPatternRec ()

			.setNumberFormat (
				numberFormatHelper.findByCode (
					GlobalId.root,
					"uk"))

			.setInputPrefix (
				"44")

			.setMinimumLength (
				12)

			.setMaximumLength (
				12)

			.setOutputPrefix (
				"44")

		);

		numberFormatPatternHelper.insert (
			new NumberFormatPatternRec ()

			.setNumberFormat (
				numberFormatHelper.findByCode (
					GlobalId.root,
					"uk"))

			.setInputPrefix (
				"+44")

			.setMinimumLength (
				13)

			.setMaximumLength (
				13)

			.setOutputPrefix (
				"44")

		);

		numberFormatPatternHelper.insert (
			new NumberFormatPatternRec ()

			.setNumberFormat (
				numberFormatHelper.findByCode (
					GlobalId.root,
					"uk"))

			.setInputPrefix (
				"0")

			.setMinimumLength (
				11)

			.setMaximumLength (
				11)

			.setOutputPrefix (
				"44")

		);

		menuItemHelper.insert (
			new MenuItemRec ()

			.setMenuGroup (
				menuGroupHelper.findByCode (
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
