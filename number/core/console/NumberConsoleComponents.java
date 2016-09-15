package wbs.sms.number.core.console;

import javax.inject.Provider;

import wbs.console.part.PagePart;
import wbs.console.request.Cryptor;
import wbs.console.request.CryptorFactory;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;

@SingletonComponent ("numberConsoleComponents")
public
class NumberConsoleComponents {

	// singleton dependencies

	@SingletonDependency
	CryptorFactory cryptorFactory;

	// prototype dependencies

	@PrototypeDependency
	Provider <NumberSubscriptionsPart> numberSubscriptionsPartProvider;

	// components

	@SingletonComponent ("numberCryptor")
	public
	Cryptor numberCryptor () {

		return cryptorFactory.makeCryptor (
			"number");

	}

	@PrototypeComponent ("numberSubscriptionsActivePart")
	public
	PagePart numberSubscriptionsActivePart () {

		return numberSubscriptionsPartProvider.get ()

			.activeOnly (
				true);

	}

}
