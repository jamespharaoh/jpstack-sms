package wbs.sms.message.stats.console;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.platform.core.console.CoreTitledResponder;

@PrototypeComponent ("smsStatsResponder")
public
class SmsStatsResponder
	extends CoreTitledResponder {

	// prototype dependencies

	@Inject
	Provider <SmsStatsPart> smsStatsPartProvider;

	@Inject
	Provider<SmsStatsSourceImpl> smsStatsSourceImplProvider;

	// init

	@PostConstruct
	public
	void init () {

		title (
			"Message stats");

		pagePart (
			smsStatsPartProvider.get ()
				.url ("/stats")
				.statsSource (smsStatsSourceImplProvider.get ()));

	}

}
