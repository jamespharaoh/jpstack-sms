package wbs.sms.gazetteer.fixture;

import static wbs.utils.string.CodeUtils.simplifyToCodeRequired;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.data.tools.DataFromXml;
import wbs.framework.data.tools.DataFromXmlBuilder;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

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

	// implementation

	@Override
	public
	void createFixtures (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"createFixtures");

		MenuGroupRec smsMenuGroup =
			menuGroupHelper.findByCodeRequired (
				GlobalId.root,
				"test",
				"sms");

		menuItemHelper.insert (
			taskLogger,
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
			new DataFromXmlBuilder ()

			.registerBuilderClasses (
				GazetteerData.class,
				GazetteerEntryData.class)

			.build ();

		GazetteerData gazetteerData =
			(GazetteerData)
			gazetteerReader.readClasspath (
				taskLogger,
				"/wbs/sms/gazetteer/fixture/gazetteer-test-data.xml");

		GazetteerRec testGazetteer =
			gazetteerHelper.insert (
				taskLogger,
				gazetteerHelper.createInstance ()

			.setSlice (
				sliceHelper.findByCodeRequired (
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
				taskLogger,
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
