package wbs.sms.gazetteer.logic;

import java.util.Collection;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;

import wbs.sms.gazetteer.model.GazetteerEntryRec;
import wbs.sms.gazetteer.model.GazetteerRec;
import wbs.sms.locator.logic.LocatorLogic;
import wbs.sms.locator.model.LongLat;

@SingletonComponent ("gazetteerLogic")
public
class GazetteerLogicImplementation
	implements GazetteerLogic {

	// singleton dependencies

	@SingletonDependency
	LocatorLogic locatorLogic;

	// implementation

	@Override
	public
	GazetteerEntryRec findNearestCanonicalEntry (
			GazetteerRec gazetteer,
			LongLat longLat) {

		Collection<GazetteerEntryRec> allEntries =
			gazetteer.getEntries ();

		GazetteerEntryRec closestEntry = null;

		double closestDistance = 0;

		for (
			GazetteerEntryRec thisEntry
				: allEntries
		) {

			if (! thisEntry.getCanonical ())
				continue;

			if (closestEntry == null) {

				closestEntry = thisEntry;

				closestDistance =
					locatorLogic.distanceMiles (
						longLat,
						thisEntry.getLongLat ());

				continue;

			}

			double thisDistance =
				locatorLogic.distanceMiles (
					longLat,
					thisEntry.getLongLat ());

			if (thisDistance < closestDistance) {

				closestEntry =
					thisEntry;

				closestDistance =
					thisDistance;

			}

		}

		return closestEntry;

	}

}
