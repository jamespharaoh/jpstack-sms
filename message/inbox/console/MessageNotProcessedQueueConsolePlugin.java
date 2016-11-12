package wbs.sms.message.inbox.console;

import lombok.NonNull;

import wbs.console.context.ConsoleContext;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.module.ConsoleManager;
import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;
import wbs.platform.queue.console.AbstractQueueConsolePlugin;
import wbs.platform.queue.model.QueueItemRec;
import wbs.web.responder.Responder;

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

	// details

	{
		queueTypeCode ("route", "not_processed");
	}

	// implementation

	@Override
	public
	Responder makeResponder (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull QueueItemRec queueItem) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"makeResponder");

		ConsoleContext targetContext =
			consoleManager.context (
				"message.notProcessed",
				true);

		consoleManager.changeContext (
			taskLogger,
			targetContext,
			"/" + queueItem.getRefObjectId ());

		return responder ("messageNotProcessedFormResponder")
			.get ();

	}

}
