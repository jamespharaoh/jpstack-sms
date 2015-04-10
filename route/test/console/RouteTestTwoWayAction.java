package wbs.sms.route.test.console;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Collections;

import javax.inject.Inject;

import lombok.Cleanup;

import org.joda.time.Instant;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.console.action.ConsoleAction;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.media.model.MediaRec;
import wbs.platform.text.model.TextObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.logic.InboxLogic;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.route.core.console.RouteConsoleHelper;
import wbs.sms.route.core.model.RouteRec;

import com.google.common.base.Optional;

@PrototypeComponent ("routeTestTwoWayAction")
public
class RouteTestTwoWayAction
	extends ConsoleAction {

	// dependencies

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

	// details

	@Override
	public
	Responder backupResponder () {
		return responder ("routeTestTwoWayResponder");
	}

	// implementation

	@Override
	public
	Responder goReal () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

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
					Optional.<String>absent (),
					textHelper.findOrCreate (messageString),
					numFrom,
					numTo,
					route,
					Optional.<NetworkRec>absent (),
					Optional.<Instant>absent (),
					Collections.<MediaRec>emptyList (),
					Optional.<String>absent (),
					Optional.<String>absent ());

			requestContext.addNotice (
				stringFormat (
					"Message %s inserted",
					messageRecord.getId ()));

		}

		transaction.commit ();

		// wait a couple of seconds for the message to be processed

		try {
			Thread.sleep (2000);
		} catch (InterruptedException exception) {
		}

		return null;

	}

}
