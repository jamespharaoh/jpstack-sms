package wbs.sms.object.stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.record.Record;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.console.lookup.ObjectLookup;
import wbs.platform.console.part.PagePart;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.sms.message.stats.console.SmsStatsCriteria;
import wbs.sms.message.stats.console.SmsStatsPart;
import wbs.sms.message.stats.console.SmsStatsSource;
import wbs.sms.message.stats.console.SmsStatsSourceImpl;

@Accessors (fluent = true)
@PrototypeComponent ("objectStatsPartFactory")
public
class ObjectStatsPartFactory
	implements Provider<PagePart> {

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	Database database;

	@Inject
	List<ObjectStatsSourceBuilder> objectStatsSourceBuilders =
		Collections.emptyList ();

	@Inject
	Provider<SmsStatsPart> objectStatsPart;

	@Inject
	Provider<SmsStatsSourceImpl> statsSourceImpl;

	@Getter @Setter
	String localName;

	@Getter @Setter
	ObjectLookup<? extends Record<?>> objectLookup;

	@Override
	public
	PagePart get () {

		@Cleanup
		Transaction transaction =
			database.beginReadOnly ();

		// lookup object

		Record<?> parent =
			objectLookup.lookupObject (
				requestContext.contextStuff ());

		// find its services

		List<SmsStatsSource> statsSources =
			new ArrayList<SmsStatsSource> ();

		for (
			ObjectStatsSourceBuilder objectStatsSourceBuilder
				: objectStatsSourceBuilders
		) {

			SmsStatsSource statsSource =
				objectStatsSourceBuilder.buildStatsSource (
					parent);

			if (statsSource == null)
				continue;

			statsSources.add (
				statsSource);

		}

		if (statsSources.isEmpty ()) {

			throw new RuntimeException (
				"No stats sources found");

		}

		if (statsSources.size () > 1) {

			throw new RuntimeException (
				"Multiple stats sources found");

		}

		SmsStatsSource statsSource =
			statsSources.get (0);

		// set up exclusions

		Set<SmsStatsCriteria> excludes =
			new HashSet<SmsStatsCriteria> ();

		// excludes.add (SmsStatsCriteria.service);

		// now create the stats part

		return objectStatsPart.get ()

			.url (
				requestContext.resolveLocalUrl (
					localName))

			.statsSource (
				statsSource)

			.excludeCriteria (
				excludes);

	}

}
