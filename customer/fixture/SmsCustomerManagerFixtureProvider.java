package wbs.sms.customer.fixture;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.record.GlobalId;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuObjectHelper;
import wbs.platform.menu.model.MenuRec;
import wbs.platform.priv.model.PrivObjectHelper;
import wbs.platform.scaffold.model.RootObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserPrivObjectHelper;

@PrototypeComponent ("smsCustomerManagerFixtureProvider")
public
class SmsCustomerManagerFixtureProvider
	implements FixtureProvider {

	// dependencies

	@Inject
	MenuGroupObjectHelper menuGroupHelper;

	@Inject
	MenuObjectHelper menuHelper;

	@Inject
	PrivObjectHelper privHelper;

	@Inject
	RootObjectHelper rootHelper;

	@Inject
	SliceObjectHelper sliceHelper;

	@Inject
	UserObjectHelper userHelper;

	@Inject
	UserPrivObjectHelper userPrivHelper;

	// implementation

	@Override
	public
	void createFixtures () {

		menuHelper.insert (
			new MenuRec ()

			.setMenuGroup (
				menuGroupHelper.findByCode (
					GlobalId.root,
					"sms"))

			.setCode (
				"customer")

			.setLabel (
				"Customers")

			.setPath (
				"/smsCustomerManagers")

		);

	}

}
