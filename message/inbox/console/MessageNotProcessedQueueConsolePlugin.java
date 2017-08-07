package wbs.sms.message.inbox.console;

import lombok.NonNull;

import wbs.console.context.ConsoleContext;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.module.ConsoleManager;
import wbs.console.priv.UserPrivChecker;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.NamedDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.OwnedTaskLogger;
import wbs.framework.logging.TaskLogger;

import wbs.platform.queue.console.AbstractQueueConsolePlugin;
import wbs.platform.queue.model.QueueItemRec;

import wbs.web.responder.WebResponder;

@PrototypeComponent ("messageNotProcessedQueueConsolePlugin")
public
class MessageNotProcessedQueueConsolePlugin
	extends AbstractQueueConsolePlugin {

	// singleton dependencies

	@SingletonDependency
	ConsoleManager consoleManager;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	UserPrivChecker privChecker;

	// prototype dependencies

	@PrototypeDependency
	@NamedDependency ("messageNotProcessedFormResponder")
	ComponentProvider <WebResponder> formResponderProvider;

	// details

	{
		queueTypeCode ("route", "not_processed");
	}

	// implementation

	@Override
	public
	WebResponder makeResponder (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull QueueItemRec queueItem) {

		try (

			OwnedTaskLogger taskLogger =
				logContext.nestTaskLogger (
					parentTaskLogger,
					"makeResponder");

		) {

			ConsoleContext targetContext =
				consoleManager.context (
					"message.notProcessed",
					true);

			consoleManager.changeContext (
				taskLogger,
				privChecker,
				targetContext,
				"/" + queueItem.getRefObjectId ());

			return formResponderProvider.provide (
				taskLogger);

		}

	}

}
