package wbs.sms.message.stats.console;

import lombok.NonNull;

import wbs.console.part.PagePart;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.StrongPrototypeDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.component.tools.ComponentFactory;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

@SingletonComponent ("messageStatsPart")
public
class MessageStatsPart
	implements ComponentFactory <PagePart> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	// prototype dependencies

	@StrongPrototypeDependency
	ComponentProvider <GenericMessageStatsPart> genericMessageStatsPartProvider;

	@PrototypeDependency
	ComponentProvider <SmsStatsSourceImplementation> smsStatsSourceProvider;

	// implementation

	@Override
	public
	PagePart makeComponent (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"makeComponent");

		) {

			return genericMessageStatsPartProvider.provide (
				taskLogger,
				genericMessageStatsPart ->
					genericMessageStatsPart

				.url (
					"/messages/message.stats")

				.statsSource (
					smsStatsSourceProvider.provide (
						taskLogger))

			);

		}

	}

}
