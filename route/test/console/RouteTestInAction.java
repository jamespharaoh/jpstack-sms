package wbs.sms.route.test.console;

import javax.inject.Inject;

import lombok.Cleanup;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.console.action.ConsoleAction;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.text.model.TextObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.logic.InboxLogic;
import wbs.sms.route.core.console.RouteConsoleHelper;
import wbs.sms.route.core.model.RouteRec;

@PrototypeComponent ("routeTestInAction")
public
class RouteTestInAction
	extends ConsoleAction {

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	Database database;

	@Inject
	InboxLogic inboxLogic;

	@Inject
	RouteConsoleHelper routeHelper;

	@Inject
	TextObjectHelper textHelper;

	@Override
	public
	Responder backupResponder () {
		return responder ("routeTestInResponder");
	}

	@Override
	public
	Responder goReal () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		int routeId =
			requestContext.stuffInt ("routeId");

		RouteRec route =
			routeHelper.find (routeId);

		MessageRec message =
			inboxLogic.inboxInsert (
				null,
				textHelper.findOrCreate (
					requestContext.parameter ("message")),
				requestContext.parameter ("num_from"),
				requestContext.parameter ("num_to"),
				route,
				null,
				null,
				null,
				null,
				null);

		transaction.commit ();

		requestContext.addNotice (
			"Message " + message.getId () + " inserted");

		return null;

	}

}
