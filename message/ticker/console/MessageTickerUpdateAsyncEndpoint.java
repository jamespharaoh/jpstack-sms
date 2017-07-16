package wbs.sms.message.ticker.console;

import lombok.NonNull;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.annotations.StrongPrototypeDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.platform.core.console.ConsoleAsyncSubscription;

import wbs.sms.message.ticker.console.MessageTickerUpdateAsyncHelper.SubscriberState;

@SingletonComponent ("messageTickerUpdateAsyncEndpoint")
public
class MessageTickerUpdateAsyncEndpoint
	implements ComponentFactory <ConsoleAsyncSubscription <SubscriberState>> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MessageTickerUpdateAsyncHelper messageTickerUpdateAsyncHelper;

	// prototype dependencies

	@StrongPrototypeDependency
	ComponentProvider <ConsoleAsyncSubscription <SubscriberState>>
		consoleAsyncSubscriptionProvider;

	// components

	@Override
	public
	ConsoleAsyncSubscription <SubscriberState> makeComponent (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"makeComponent");

		) {

			return consoleAsyncSubscriptionProvider.provide (
				taskLogger,
				consoleAsyncSubscription ->
					consoleAsyncSubscription

				.helper (
					messageTickerUpdateAsyncHelper)

			);

		}

	}

}
