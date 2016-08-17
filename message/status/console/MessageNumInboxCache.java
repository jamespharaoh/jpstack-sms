package wbs.sms.message.status.console;

import javax.inject.Inject;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.platform.misc.CachedGetter;
import wbs.sms.message.inbox.model.InboxObjectHelper;

@SingletonComponent ("messageNumInboxCache")
public
class MessageNumInboxCache
	extends CachedGetter <Long> {

	@Inject
	InboxObjectHelper inboxHelper;

	public
	MessageNumInboxCache () {

		super (1000);

	}

	@Override
	public
	Long refresh () {

		return inboxHelper.countPending ();

	}

}
