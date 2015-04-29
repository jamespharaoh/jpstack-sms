package wbs.sms.message.core.console;

import static wbs.framework.utils.etc.Misc.notIn;

import javax.inject.Inject;
import javax.servlet.ServletException;

import lombok.Cleanup;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.console.action.ConsoleAction;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.console.UserConsoleHelper;
import wbs.platform.user.model.UserRec;
import wbs.sms.message.core.logic.MessageLogic;
import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;

@PrototypeComponent ("messageActionsAction")
public
class MessageActionsAction
	extends ConsoleAction {

	// dependencies

	@Inject
	Database database;

	@Inject
	EventLogic eventLogic;

	@Inject
	MessageConsoleHelper messageHelper;

	@Inject
	MessageLogic messageLogic;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	UserConsoleHelper userHelper;

	// details

	@Override
	protected
	Responder backupResponder () {

		return responder (
			"messageActionsResponder");

	}

	// implementation

	@Override
	protected
	Responder goReal ()
		throws ServletException {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		UserRec myUser =
			userHelper.find (
				requestContext.userId ());

		MessageRec message =
			messageHelper.find (
				requestContext.stuffInt (
					"messageId"));

		if (requestContext.parameter ("manuallyUndeliver") != null) {

			if (message.getDirection () != MessageDirection.out) {
				throw new RuntimeException ();
			}

			if (
				notIn (
					message.getStatus (),
					MessageStatus.sent,
					MessageStatus.submitted,
					MessageStatus.delivered)
			) {

				requestContext.addError (
					"Message in invalid state for this operation");

				return null;

			}

			messageLogic.messageStatus (
				message,
				MessageStatus.manuallyUndelivered);

			eventLogic.createEvent (
				"message_manually_undelivered",
				myUser,
				message);

			transaction.commit ();

			requestContext.addNotice (
				"Message manually undelivered");

			return null;

		} else {

			throw new RuntimeException ();

		}

	}

}
