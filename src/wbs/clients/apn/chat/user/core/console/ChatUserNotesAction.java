package wbs.clients.apn.chat.user.core.console;

import static wbs.framework.utils.etc.Misc.stringTrim;
import static wbs.framework.utils.etc.StringUtils.stringIsEmpty;

import lombok.Cleanup;

import wbs.clients.apn.chat.user.core.model.ChatUserNoteObjectHelper;
import wbs.clients.apn.chat.user.core.model.ChatUserRec;
import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.service.model.ServiceObjectHelper;
import wbs.platform.text.model.TextObjectHelper;
import wbs.platform.text.model.TextRec;
import wbs.platform.user.console.UserConsoleLogic;
import wbs.platform.user.model.UserObjectHelper;

@PrototypeComponent ("chatUserNotesAction")
public
class ChatUserNotesAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@SingletonDependency
	ChatUserNoteObjectHelper chatUserNoteHelper;

	@SingletonDependency
	Database database;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	ServiceObjectHelper serviceHelper;

	@SingletonDependency
	TextObjectHelper textHelper;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	@SingletonDependency
	UserObjectHelper userHelper;

	// details

	@Override
	public
	Responder backupResponder () {
		return responder ("chatUserNotesResponder");
	}

	// implementation

	@Override
	protected
	Responder goReal () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ChatUserNotesAction.goReal ()",
				this);

		ChatUserRec chatUser =
			chatUserHelper.findRequired (
				requestContext.stuffInteger (
					"chatUserId"));

		// check params

		String noteString =
			stringTrim (
				requestContext.parameterRequired (
					"note"));

		if (
			stringIsEmpty (
				noteString)
		) {

			requestContext.addError (
				"Please enter a note");

			return null;

		}

		// create note

		TextRec noteText =
			textHelper.findOrCreate (noteString);

		chatUserNoteHelper.insert (
			chatUserNoteHelper.createInstance ()

			.setChatUser (
				chatUser)

			.setTimestamp (
				transaction.now ())

			.setUser (
				userConsoleLogic.userRequired ())

			.setText (
				noteText)

		);

		// wrap up

		transaction.commit ();

		requestContext.addNotice (
			"Note added");

		return null;

	}

}
