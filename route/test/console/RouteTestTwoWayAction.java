package wbs.sms.route.test.console;

import static wbs.utils.string.StringUtils.stringFormatObsolete;

import java.util.Collections;

import com.google.common.base.Optional;

import lombok.Cleanup;
import lombok.NonNull;

import org.joda.time.Instant;

import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.logging.TaskLogger;
import wbs.platform.media.model.MediaRec;
import wbs.platform.text.model.TextObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.logic.SmsInboxLogic;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.route.core.console.RouteConsoleHelper;
import wbs.sms.route.core.model.RouteRec;
import wbs.web.responder.Responder;

@PrototypeComponent ("routeTestTwoWayAction")
public
class RouteTestTwoWayAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	Database database;

	@SingletonDependency
	SmsInboxLogic smsInboxLogic;

	@SingletonDependency
	RouteConsoleHelper routeHelper;

	@SingletonDependency
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
	Responder goReal (
			@NonNull TaskLogger taskLogger) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"RouteTestTwoWayAction.goReal ()",
				this);

		RouteRec route =
			routeHelper.findRequired (
				requestContext.stuffInteger (
					"routeId"));

		String messageString =
			requestContext.parameterRequired (
				"message");

		String numFrom =
			requestContext.parameterRequired (
				"num_from");

		String numTo =
			requestContext.parameterRequired (
				"num_to");

		MessageRec messageRecord =
			smsInboxLogic.inboxInsert (
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
			stringFormatObsolete (
				"Message %s inserted",
				messageRecord.getId ()));

		transaction.commit ();

		// wait a couple of seconds for the message to be processed

		try {
			Thread.sleep (2000);
		} catch (InterruptedException exception) {
		}

		return null;

	}

}
