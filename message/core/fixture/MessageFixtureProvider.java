package wbs.sms.message.core.fixture;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

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
			@NonNull TaskLogger parentTaskLogger,
			@NonNull OwnedTransaction transaction) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"createFixtures");

		) {

			createMenuItems (
				taskLogger,
				transaction);

		}

	}

	private
	void createMenuItems (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull OwnedTransaction transaction) {

		try (

			TaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"createMenuItems");

		) {

			menuItemHelper.insert (
				taskLogger,
				menuItemHelper.createInstance ()

				.setMenuGroup (
					menuGroupHelper.findByCodeRequired (
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
				taskLogger,
				menuItemHelper.createInstance ()

				.setMenuGroup (
					menuGroupHelper.findByCodeRequired (
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
