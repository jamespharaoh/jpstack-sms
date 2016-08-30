package wbs.sms.message.status.console;

import org.joda.time.Duration;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.application.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.misc.CachedGetter;
import wbs.sms.message.inbox.model.InboxObjectHelper;

@SingletonComponent ("messageNumInboxCache")
public
class MessageNumInboxCache
	extends CachedGetter <Long> {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@SingletonDependency
	InboxObjectHelper inboxHelper;

	// constructors

	public
	MessageNumInboxCache () {

		super (1000);

	}

	// implementation

	@Override
	public
	Long refresh () {

		Transaction transaction =
			database.currentTransaction ();

		return inboxHelper.countPendingOlderThan (
			transaction.now ().minus (
				Duration.standardSeconds (
					5)));

	}

}
