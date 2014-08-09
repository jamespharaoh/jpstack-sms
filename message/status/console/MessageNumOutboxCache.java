package wbs.sms.message.status.console;

import javax.inject.Inject;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.platform.misc.CachedGetter;
import wbs.sms.message.outbox.model.OutboxObjectHelper;

@SingletonComponent ("messageNumOutboxCache")
public
class MessageNumOutboxCache
	extends CachedGetter<Integer> {

	@Inject
	OutboxObjectHelper outboxHelper;

	public
	MessageNumOutboxCache () {
		super (1000);
	}

	@Override
	public
	Integer refresh () {

		return outboxHelper.count ();

	}

}
