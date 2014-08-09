package wbs.sms.number.core.console;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.platform.console.part.PagePart;
import wbs.platform.console.request.Cryptor;

@SingletonComponent ("numberConsoleConfig")
public
class NumberConsoleConfig {

	@Inject
	Provider<NumberSubscriptionsPart> numberSubscriptionsPart;

	@SingletonComponent ("numberCryptor")
	public
	Cryptor numberCryptor () {
		return new Cryptor ();
	}

	@PrototypeComponent ("numberSubscriptionActivePart")
	public
	PagePart numberSubscriptionsActivePart () {

		return numberSubscriptionsPart.get ()
			.activeOnly (true);

	}

}
