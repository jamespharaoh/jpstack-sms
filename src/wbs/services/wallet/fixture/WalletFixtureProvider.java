package wbs.services.wallet.fixture;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.config.WbsConfig;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.logging.LogContext;

import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;

import wbs.utils.random.RandomLogic;

import wbs.services.wallet.model.WalletObjectHelper;
import wbs.services.wallet.model.WalletServiceObjectHelper;
import wbs.services.wallet.model.WalletServiceRec;

@PrototypeComponent ("walletFixtureProvider")
public
class WalletFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MenuGroupObjectHelper menuGroupHelper;

	@SingletonDependency
	MenuItemObjectHelper menuItemHelper;

	@SingletonDependency
	RandomLogic randomLogic;

	@SingletonDependency
	SliceObjectHelper sliceHelper;

	@SingletonDependency
	WalletObjectHelper walletHelper;

	@SingletonDependency
	WalletServiceObjectHelper walletServiceHelper;

	@SingletonDependency
	WbsConfig wbsConfig;

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

			menuItemHelper.insert (
				transaction,
				menuItemHelper.createInstance ()

				.setMenuGroup (
					menuGroupHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						wbsConfig.defaultSlice (),
						"facility"))

				.setCode (
					"wallet_service")

				.setName (
					"Wallet service")

				.setDescription (
					"")

				.setLabel (
					"Wallet Service")

				.setTargetPath (
					"/walletServices")

				.setTargetFrame (
					"main")

			);

			WalletServiceRec walletService =
				walletServiceHelper.insert (
					transaction,
					walletServiceHelper.createInstance ()

				.setSlice (
					sliceHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						wbsConfig.defaultSlice ()))

				.setCode (
					"test_wallet_service")

				.setName (
					"Test wallet service")

				.setDescription (
					"Test wallet service")

			);

			walletHelper.insert (
				transaction,
				walletHelper.createInstance ()

				.setWalletService (
					walletService)

				.setCode (
					randomLogic.generateNumericNoZero (8))

			);

		}

	}

}

