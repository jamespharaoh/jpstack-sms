package wbs.sms.number.core.console;

import javax.inject.Inject;
import javax.inject.Provider;

import wbs.console.part.PagePart;
import wbs.console.request.Cryptor;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.annotations.SingletonComponent;

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

	@PrototypeComponent ("numberSubscriptionsActivePart")
	public
	PagePart numberSubscriptionsActivePart () {

		return numberSubscriptionsPart.get ()

			.activeOnly (
				true);

	}

}
