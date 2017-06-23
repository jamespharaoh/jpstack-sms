package wbs.sms.keyword.fixture;

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

import wbs.sms.keyword.model.KeywordSetObjectHelper;
import wbs.sms.keyword.model.KeywordSetType;

@PrototypeComponent ("keywordFixtureProvider")
public
class KeywordFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@SingletonDependency
	KeywordSetObjectHelper keywordSetHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MenuGroupObjectHelper menuGroupHelper;

	@SingletonDependency
	MenuItemObjectHelper menuItemHelper;

	@SingletonDependency
	SliceObjectHelper sliceHelper;

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

			keywordSetHelper.insert (
				transaction,
				keywordSetHelper.createInstance ()

				.setSlice (
					sliceHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						wbsConfig.defaultSlice ()))

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
				transaction,
				menuItemHelper.createInstance ()

				.setMenuGroup (
					menuGroupHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						wbsConfig.defaultSlice (),
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

}
