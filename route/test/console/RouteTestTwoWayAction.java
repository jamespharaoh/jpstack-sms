package wbs.sms.route.test.console;

import static wbs.framework.utils.etc.Misc.stringFormat;

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

@PrototypeComponent ("routeTestTwoWayAction")
public
class RouteTestTwoWayAction
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
		return responder ("routeTestTwoWayResponder");
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

		String messageString =
			requestContext.parameter ("message");

		String numFrom =
			requestContext.parameter ("num_from");

		String numTo =
			requestContext.parameter ("num_to");

		if (
			messageString != null
			&& messageString.length () > 0
			&& numFrom != null
			&& numFrom.length () > 0
			&& numTo != null
			&& numTo.length() > 0
		) {

			MessageRec messageRecord =
				inboxLogic.inboxInsert (
					null,
					textHelper.findOrCreate (messageString),
					numFrom,
					numTo,
					route,
					null,
					null,
					null,
					null,
					null);

			requestContext.addNotice (
				stringFormat (
					"Message %s inserted",
					messageRecord.getId ()));

			// wait a couple of seconds for the message to be processed
			// TODO say what?

			try {

				Thread.sleep (2000);

			} catch (InterruptedException exception) {

			}

		}

		transaction.commit ();

		return null;

	}

}
