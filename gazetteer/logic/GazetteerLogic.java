package wbs.sms.gazetteer.logic;

import wbs.sms.gazetteer.model.GazetteerEntryRec;
import wbs.sms.gazetteer.model.GazetteerRec;
import wbs.sms.locator.model.LongLat;

public
interface GazetteerLogic {

	GazetteerEntryRec findNearestCanonicalEntry (
			GazetteerRec gazetteer,
			LongLat longLat);

}
