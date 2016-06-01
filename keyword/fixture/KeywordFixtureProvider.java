package wbs.sms.keyword.fixture;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.record.GlobalId;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.sms.keyword.model.KeywordSetObjectHelper;
import wbs.sms.keyword.model.KeywordSetType;

@PrototypeComponent ("keywordFixtureProvider")
public
class KeywordFixtureProvider
	implements FixtureProvider {

	// dependencies

	@Inject
	KeywordSetObjectHelper keywordSetHelper;

	@Inject
	MenuGroupObjectHelper menuGroupHelper;

	@Inject
	MenuItemObjectHelper menuItemHelper;

	@Inject
	SliceObjectHelper sliceHelper;

	// implementation

	@Override
	public
	void createFixtures () {

		keywordSetHelper.insert (
			keywordSetHelper.createInstance ()

			.setSlice (
				sliceHelper.findByCodeRequired (
					GlobalId.root,
					"test"))

			.setCode (
				"inbound")

			.setName (
				"inbound")

			.setDescription (
				"Inbound")

			.setType (
				KeywordSetType.keyword)

		);

		menuItemHelper.insert (
			menuItemHelper.createInstance ()

			.setMenuGroup (
				menuGroupHelper.findByCodeRequired (
					GlobalId.root,
					"test",
					"sms"))

			.setCode (
				"keyword_set")

			.setName (
				"Keyword Set")

			.setDescription (
				"Route inbound messages based on keyword")

			.setLabel (
				"Keyword sets")

			.setTargetPath (
				"/keywordSets")

			.setTargetFrame (
				"main")

		);

	}

}
