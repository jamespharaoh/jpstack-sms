package wbs.sms.message.status.console;

import javax.inject.Inject;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.platform.misc.CachedGetter;
import wbs.sms.message.core.model.MessageObjectHelper;

@SingletonComponent ("messageNumNotProcessedCache")
public
class MessageNumNotProcessedCache
	extends CachedGetter<Integer> {

	@Inject
	MessageObjectHelper messageHelper;

	public
	MessageNumNotProcessedCache () {
		super (1000);
	}

	@Override
	public
	Integer refresh () {

		return messageHelper.countNotProcessed ();

	}

}
