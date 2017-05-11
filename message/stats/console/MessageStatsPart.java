package wbs.sms.message.stats.console;

import javax.inject.Provider;

import lombok.NonNull;

import wbs.console.part.PagePart;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.UninitializedDependency;
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

	@PrototypeDependency
	Provider <SmsStatsSourceImplementation> smsStatsSourceProvider;

	// uninitialized dependencies

	@UninitializedDependency
	Provider <GenericMessageStatsPart> genericMessageStatsPartProvider;

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

			return genericMessageStatsPartProvider.get ()

				.url (
					"/messages/message.stats")

				.statsSource (
					smsStatsSourceProvider.get ())

			;

		}

	}

}
