package wbs.clients.apn.chat.settings.console;

import lombok.Cleanup;

import wbs.clients.apn.chat.core.logic.ChatMiscLogic;
import wbs.clients.apn.chat.core.model.ChatObjectHelper;
import wbs.clients.apn.chat.core.model.ChatRec;
import wbs.clients.apn.chat.user.core.model.Gender;
import wbs.clients.apn.chat.user.core.model.Orient;
import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.application.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;

@PrototypeComponent ("chatSettingsMonitorsAction")
public
class ChatSettingsMonitorsAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	ChatMiscLogic chatMiscLogic;

	@SingletonDependency
	ChatObjectHelper chatHelper;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	Database database;

	// details

	@Override
	public
	Responder backupResponder () {
		return responder ("chatSettingsMonitorsResponder");
	}

	@Override
	public
	Responder goReal () {

		if (! requestContext.canContext ("chat.manage")) {

			requestContext.addError ("Access denied");

			return null;

		}

		long gayMale;
		long gayFemale;
		long biMale;
		long biFemale;
		long straightMale;
		long straightFemale;

		try {

			gayMale =
				requestContext.parameterIntegerRequired (
					"gayMale");

			gayFemale =
				requestContext.parameterIntegerRequired (
					"gayFemale");

			biMale =
				requestContext.parameterIntegerRequired (
					"biMale");

			biFemale =
				requestContext.parameterIntegerRequired (
					"biFemale");

			straightMale =
				requestContext.parameterIntegerRequired (
					"straightMale");

			straightFemale =
				requestContext.parameterIntegerRequired (
					"straightFemale");

		} catch (NumberFormatException exception) {

			requestContext.addError (
				"Please enter a real number in each box.");

			return null;

		}

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"ChatSettingsMonitorsAction.goReal ()",
				this);

		ChatRec chat =
			chatHelper.findRequired (
				requestContext.stuffInteger (
					"chatId"));

		chatMiscLogic.monitorsToTarget (
			chat,
			Gender.male,
			Orient.gay,
			gayMale);

		chatMiscLogic.monitorsToTarget (
			chat,
			Gender.female,
			Orient.gay,
			gayFemale);

		chatMiscLogic.monitorsToTarget (
			chat,
			Gender.male,
			Orient.bi,
			biMale);

		chatMiscLogic.monitorsToTarget (
			chat,
			Gender.female,
			Orient.bi,
			biFemale);

		chatMiscLogic.monitorsToTarget (
			chat,
			Gender.male,
			Orient.straight,
			straightMale);

		chatMiscLogic.monitorsToTarget (
			chat,
			Gender.female,
			Orient.straight,
			straightFemale);

		transaction.commit ();

		requestContext.addNotice (
			"Chat monitors updated");

		requestContext.setEmptyFormData ();

		return null;

	}

}
