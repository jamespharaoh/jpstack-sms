package wbs.sms.object.stats;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Provider;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.lookup.ObjectLookup;
import wbs.console.part.PagePart;
import wbs.console.part.PagePartFactory;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.sms.message.stats.console.GenericMessageStatsPart;
import wbs.sms.message.stats.console.SmsStatsCriteria;
import wbs.sms.message.stats.console.SmsStatsSource;
import wbs.sms.message.stats.console.SmsStatsSourceImplementation;

@Accessors (fluent = true)
@PrototypeComponent ("objectStatsPartFactory")
public
class ObjectStatsPartFactory
	implements PagePartFactory {

	// singelton dependencies

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	List <ObjectStatsSourceBuilder> objectStatsSourceBuilders;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// prototype dependencies

	@PrototypeDependency
	Provider <GenericMessageStatsPart> smsStatsPartProvider;

	@PrototypeDependency
	Provider <SmsStatsSourceImplementation> smsStatsSourceProvider;

	// properties

	@Getter @Setter
	String localName;

	@Getter @Setter
	ObjectLookup<? extends Record<?>> objectLookup;

	// implementation

	@Override
	public
	PagePart buildPagePart (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"buildPagePart");

		try (

			Transaction transaction =
				database.beginReadOnly (
					taskLogger,
					"ObjectStatsPartFactory.get ()",
					this);

		) {

			// lookup object

			Record <?> parent =
				objectLookup.lookupObject (
					requestContext.consoleContextStuffRequired ());

			// find its services

			List <SmsStatsSource> statsSources =
				new ArrayList<> ();

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

			return smsStatsPartProvider.get ()

				.url (
					requestContext.resolveLocalUrl (
						localName))

				.statsSource (
					statsSource)

				.excludeCriteria (
					excludes);

		}

	}

}
