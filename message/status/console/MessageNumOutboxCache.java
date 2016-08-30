package wbs.sms.message.status.console;

import javax.inject.Inject;

import org.joda.time.Duration;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.application.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.misc.CachedGetter;
import wbs.sms.message.outbox.model.OutboxObjectHelper;

@SingletonComponent ("messageNumOutboxCache")
public
class MessageNumOutboxCache
	extends CachedGetter <Long> {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@Inject
	OutboxObjectHelper outboxHelper;

	// constructors

	public
	MessageNumOutboxCache () {
		super (1000);
	}

	// implementation

	@Override
	public
	Long refresh () {

		Transaction transaction =
			database.currentTransaction ();

		return outboxHelper.countOlderThan (
			transaction.now ().minus (
				Duration.standardSeconds (
					5)));

	}

}
