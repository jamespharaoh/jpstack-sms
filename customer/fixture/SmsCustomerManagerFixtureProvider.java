package wbs.sms.customer.fixture;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.record.GlobalId;
import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.menu.model.MenuItemRec;
import wbs.platform.priv.model.PrivObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;
import wbs.platform.user.model.UserObjectHelper;
import wbs.platform.user.model.UserPrivObjectHelper;
import wbs.sms.customer.model.SmsCustomerManagerObjectHelper;
import wbs.sms.customer.model.SmsCustomerManagerRec;

@PrototypeComponent ("smsCustomerManagerFixtureProvider")
public
class SmsCustomerManagerFixtureProvider
	implements FixtureProvider {

	// dependencies

	@Inject
	MenuGroupObjectHelper menuGroupHelper;

	@Inject
	MenuItemObjectHelper menuItemHelper;

	@Inject
	PrivObjectHelper privHelper;

	@Inject
	SliceObjectHelper sliceHelper;

	@Inject
	SmsCustomerManagerObjectHelper smsCustomerManagerHelper;

	@Inject
	UserObjectHelper userHelper;

	@Inject
	UserPrivObjectHelper userPrivHelper;

	// implementation

	@Override
	public
	void createFixtures () {

		smsCustomerManagerHelper.insert (
			new SmsCustomerManagerRec ()

			.setSlice (
				sliceHelper.findByCode (
					GlobalId.root,
					"test"))

			.setCode (
				"customer_manager")

			.setName (
				"Customer manager")

			.setDescription (
				"Test customer manager")

		);

		menuItemHelper.insert (
			new MenuItemRec ()

			.setMenuGroup (
				menuGroupHelper.findByCode (
					GlobalId.root,
					"test",
					"sms"))

			.setCode (
				"customer")

			.setName (
				"Customer")

			.setDescription (
				"")

			.setLabel (
				"Customers")

			.setTargetPath (
				"/smsCustomerManagers")

			.setTargetFrame (
				"main")

		);

	}

}
