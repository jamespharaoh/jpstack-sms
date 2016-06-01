package wbs.sms.gazetteer.fixture;

import static wbs.framework.utils.etc.CodeUtils.simplifyToCodeRequired;

import javax.inject.Inject;

import com.google.common.collect.ImmutableList;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.data.tools.DataFromXml;
import wbs.framework.fixtures.FixtureProvider;
import wbs.framework.record.GlobalId;
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

	// dependencies

	@Inject
	GazetteerEntryObjectHelper gazetteerEntryHelper;

	@Inject
	GazetteerObjectHelper gazetteerHelper;

	@Inject
	MenuGroupObjectHelper menuGroupHelper;

	@Inject
	MenuItemObjectHelper menuItemHelper;

	@Inject
	SliceObjectHelper sliceHelper;

	// implementation

	@Override
	public
	void createFixtures () {

		MenuGroupRec smsMenuGroup =
			menuGroupHelper.findByCodeRequired (
				GlobalId.root,
				"test",
				"sms");

		menuItemHelper.insert (
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
			new DataFromXml ()

			.registerBuilderClasses (
				GazetteerData.class,
				GazetteerEntryData.class);

		GazetteerData gazetteerData =
			(GazetteerData)
			gazetteerReader.readClasspath (
				ImmutableList.of (),
				"/wbs/sms/gazetteer/fixture/gazetteer-test-data.xml");

		GazetteerRec testGazetteer =
			gazetteerHelper.insert (
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
