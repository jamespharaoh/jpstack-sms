package wbs.sms.core.fixture;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;

@PrototypeComponent ("smsCoreFixtureProvider")
public
class SmsCoreFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@SingletonDependency
	MenuGroupObjectHelper menuGroupHelper;

	@SingletonDependency
	SliceObjectHelper sliceHelper;

	// implementation

	@Override
	public
	void createFixtures () {

		menuGroupHelper.insert (
			menuGroupHelper.createInstance ()

			.setSlice (
				sliceHelper.findByCodeRequired (
					GlobalId.root,
					"test"))

			.setCode (
				"sms")

			.setName (
				"SMS")

			.setDescription (
				"")

			.setLabel (
				"SMS")

			.setOrder (
				30l)

		);

	}

}
