package wbs.sms.message.core.fixture;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.logging.LogContext;

import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;

@PrototypeComponent ("messageFixtureProvider")
public
class MessageFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MenuGroupObjectHelper menuGroupHelper;

	@SingletonDependency
	MenuItemObjectHelper menuItemHelper;

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

			createMenuItems (
				transaction);

		}

	}

	private
	void createMenuItems (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"createMenuItems");

		) {

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
					"message")

				.setName (
					"Message")

				.setDescription (
					"")

				.setLabel (
					"Messages")

				.setTargetPath (
					"/messages")

				.setTargetFrame (
					"main")

			);

			menuItemHelper.insert (
				transaction,
				menuItemHelper.createInstance ()

				.setMenuGroup (
					menuGroupHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						"test",
						"panel"))

				.setCode (
					"message_ticker")

				.setName (
					"Message ticker")

				.setDescription (
					"")

				.setLabel (
					"Message ticker")

				.setTargetPath (
					"/messageTicker/messageTicker.ticker")

				.setTargetFrame (
					"inbox")

			);

			transaction.flush ();

		}

	}

}
