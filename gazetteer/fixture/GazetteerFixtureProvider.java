package wbs.sms.gazetteer.fixture;

import static wbs.utils.string.CodeUtils.simplifyToCodeRequired;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.data.tools.DataFromXml;
import wbs.framework.data.tools.DataFromXmlBuilder;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.logging.LogContext;

import wbs.platform.menu.model.MenuGroupObjectHelper;
import wbs.platform.menu.model.MenuGroupRec;
import wbs.platform.menu.model.MenuItemObjectHelper;
import wbs.platform.scaffold.model.SliceObjectHelper;

import wbs.sms.gazetteer.model.GazetteerData;
import wbs.sms.gazetteer.model.GazetteerEntryData;
import wbs.sms.gazetteer.model.GazetteerEntryObjectHelper;
import wbs.sms.gazetteer.model.GazetteerObjectHelper;
import wbs.sms.gazetteer.model.GazetteerRec;
import wbs.sms.locator.model.LongLat;

@PrototypeComponent ("gazetteerFixtureProvider")
public
class GazetteerFixtureProvider
	implements FixtureProvider {

	// singleton dependencies

	@SingletonDependency
	GazetteerEntryObjectHelper gazetteerEntryHelper;

	@SingletonDependency
	GazetteerObjectHelper gazetteerHelper;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MenuGroupObjectHelper menuGroupHelper;

	@SingletonDependency
	MenuItemObjectHelper menuItemHelper;

	@SingletonDependency
	SliceObjectHelper sliceHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider <DataFromXmlBuilder> dataFromXmlBuilderProvider;

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

			MenuGroupRec smsMenuGroup =
				menuGroupHelper.findByCodeRequired (
					transaction,
					GlobalId.root,
					"test",
					"sms");

			menuItemHelper.insert (
				transaction,
				menuItemHelper.createInstance ()

				.setMenuGroup (
					smsMenuGroup)

				.setCode (
					"gazetteer")

				.setName (
					"Gazetteer")

				.setDescription (
					"Gazetteer")

				.setLabel (
					"Gazetteer")

				.setTargetPath (
					"/gazetteers")

				.setTargetFrame (
					"main")

			);

			DataFromXml gazetteerReader =
				dataFromXmlBuilderProvider.get ()

				.registerBuilderClasses (
					transaction,
					GazetteerData.class,
					GazetteerEntryData.class)

				.build (
					transaction)

			;

			GazetteerData gazetteerData =
				(GazetteerData)
				gazetteerReader.readClasspathRequired (
					transaction,
					"/wbs/sms/gazetteer/fixture/gazetteer-test-data.xml");

			GazetteerRec testGazetteer =
				gazetteerHelper.insert (
					transaction,
					gazetteerHelper.createInstance ()

				.setSlice (
					sliceHelper.findByCodeRequired (
						transaction,
						GlobalId.root,
						"test"))

				.setCode (
					"test")

				.setName (
					"Test")

				.setDescription (
					"Leeds postcodes for test purposes")

				.setDeleted (
					false)

			);

			for (
				GazetteerEntryData entryData
					: gazetteerData.entries ()
			) {

				gazetteerEntryHelper.insert (
					transaction,
					gazetteerEntryHelper.createInstance ()

					.setGazetteer (
						testGazetteer)

					.setCode (
						simplifyToCodeRequired (
							entryData.name ()))

					.setName (
						entryData.name ())

					.setDescription (
						"")

					.setDeleted (
						false)

					.setLongLat (
						LongLat.parseRequired (
							entryData.value ()))

					.setCanonical (
						entryData.canonical ())

				);

			}

		}

	}

}
