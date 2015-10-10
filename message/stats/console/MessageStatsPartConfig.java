package wbs.sms.message.stats.console;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.console.part.PagePart;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.annotations.SingletonComponent;

@SingletonComponent ("messageStatsPartConfig")
public
class MessageStatsPartConfig {

	// prototype dependencies

	@Inject
	Provider<GenericMessageStatsPart> genericMessageStatsPartProvider;

	@Inject
	Provider<SmsStatsSourceImpl> smsStatsSourceImplProvider;

	// init

	@PrototypeComponent ("messageStatsPart")
	public
	PagePart messageStatsPart () {

		return genericMessageStatsPartProvider.get ()

			.url (
				"/messages/message.stats")

			.statsSource (
				smsStatsSourceImplProvider.get ());

	}

}
