package wbs.sms.message.status.console;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.Duration;
import org.joda.time.Instant;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;

import wbs.platform.misc.CachedGetter;
import wbs.platform.scaffold.console.SliceConsoleHelper;
import wbs.platform.scaffold.model.SliceRec;

import wbs.sms.message.inbox.console.InboxConsoleHelper;
import wbs.sms.route.core.console.RouteConsoleHelper;
import wbs.sms.route.core.model.RouteRec;

@Accessors (fluent = true)
@PrototypeComponent ("messageNumInboxCache")
public
class MessageNumInboxCache
	extends CachedGetter <Long> {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	InboxConsoleHelper inboxHelper;

	@SingletonDependency
	RouteConsoleHelper routeHelper;

	@SingletonDependency
	SliceConsoleHelper sliceHelper;

	// properties

	@Getter @Setter
	Long sliceId;

	// constructors

	public
	MessageNumInboxCache () {

		super (5000);

	}

	// implementation

	@Override
	public
	Long refresh () {

		Transaction transaction =
			database.currentTransaction ();

		SliceRec slice =
			sliceHelper.findRequired (
				sliceId);

		List <RouteRec> routes =
			routeHelper.findByParent (
				slice);

		Instant olderThan =
			transaction.now ().minus (
				Duration.standardSeconds (
					5));

		return routes.stream ()

			.mapToLong (
				route ->
					inboxHelper.countPendingOlderThan (
						route,
						olderThan))

			.sum ();

	}

}
