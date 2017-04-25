package wbs.sms.message.ticker.console;

import javax.inject.Provider;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.UninitializedDependency;

import wbs.platform.core.console.ConsoleAsyncSubscription;

import wbs.sms.message.ticker.console.MessageTickerUpdateAsyncHelper.SubscriberState;

@SingletonComponent ("messageTickerUpdateAsyncConfig")
public
class MessageTickerUpdateAsyncConfig {

	// singleton dependencies

	@SingletonDependency
	MessageTickerUpdateAsyncHelper messageTickerUpdateAsyncHelper;

	// unitialized dependencies

	@UninitializedDependency
	Provider <ConsoleAsyncSubscription <SubscriberState>>
	consoleAsyncSubscriptionProvider;

	// components

	@SingletonComponent ("messageTickerUpdateAsyncEndpoint")
	public
	ConsoleAsyncSubscription <SubscriberState>
	messageTickerUpdateAsyncEndpoint () {

		return consoleAsyncSubscriptionProvider.get ()

			.helper (
				messageTickerUpdateAsyncHelper)

		;

	}

}
