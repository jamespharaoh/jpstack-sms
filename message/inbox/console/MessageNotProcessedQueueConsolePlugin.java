package wbs.sms.message.inbox.console;

import javax.inject.Inject;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.web.Responder;
import wbs.platform.console.context.ConsoleContext;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.console.module.ConsoleManager;
import wbs.platform.queue.console.AbstractQueueConsolePlugin;
import wbs.platform.queue.model.QueueItemRec;
import wbs.platform.queue.model.QueueRec;

@PrototypeComponent ("messageNotProcessedQueueConsolePlugin")
public
class MessageNotProcessedQueueConsolePlugin
	extends AbstractQueueConsolePlugin {

	// dependencies

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	ConsoleManager consoleManager;

	// details

	{
		queueTypeCode ("route", "not_processed");
	}

	// implementation

	@Override
	public
	Responder makeResponder (
			QueueItemRec queueItem) {

		ConsoleContext targetContext =
			consoleManager.context (
				"message.notProcessed",
				true);

		consoleManager.changeContext (
			targetContext,
			"/" + queueItem.getRefObjectId ());

		return responder ("messageNotProcessedFormResponder")
			.get ();

	}

	@Override
	public
	long preferredUserDelay (
			QueueRec queue) {

		return 0L;

	}

}
