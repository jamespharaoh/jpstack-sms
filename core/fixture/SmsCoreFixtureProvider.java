package wbs.sms.core.fixture;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuGroupRec;

@PrototypeComponent ("smsCoreFixtureProvider")
public
class SmsCoreFixtureProvider
	implements FixtureProvider {

	// dependencies

	@Inject
	MenuGroupObjectHelper menuGroupHelper;

	// implementation

	@Override
	public
	void createFixtures () {

		menuGroupHelper.insert (
			new MenuGroupRec ()

			.setCode (
				"sms")

			.setLabel (
				"SMS")

			.setOrder (
				30)

		);

	}

}
