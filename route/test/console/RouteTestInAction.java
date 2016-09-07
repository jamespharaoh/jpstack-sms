package wbs.sms.route.test.console;

import java.util.Collections;

import javax.inject.Inject;

import org.joda.time.Instant;

import com.google.common.base.Optional;

import lombok.Cleanup;
import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.web.Responder;
import wbs.platform.media.model.MediaRec;
import wbs.platform.text.model.TextObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.logic.SmsInboxLogic;
import wbs.sms.network.model.NetworkRec;
import wbs.sms.route.core.console.RouteConsoleHelper;
import wbs.sms.route.core.model.RouteRec;

@PrototypeComponent ("routeTestInAction")
public
class RouteTestInAction
	extends ConsoleAction {

	// dependencies

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	Database database;

	@Inject
	SmsInboxLogic smsInboxLogic;

	@Inject
	RouteConsoleHelper routeHelper;

	@Inject
	TextObjectHelper textHelper;

	// details

	@Override
	public
	Responder backupResponder () {
		return responder ("routeTestInResponder");
	}

	// implementation

	@Override
	public
	Responder goReal () {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"RouteTestInAction.goReal ()",
				this);

		Long routeId =
			requestContext.stuffInteger (
				"routeId");

		RouteRec route =
			routeHelper.findRequired (
				routeId);

		MessageRec message =
			smsInboxLogic.inboxInsert (
				Optional.<String>absent (),
				textHelper.findOrCreate (
					requestContext.parameterRequired (
						"message")),
				requestContext.parameterRequired (
					"num_from"),
				requestContext.parameterRequired (
					"num_to"),
				route,
				Optional.<NetworkRec>absent (),
				Optional.<Instant>absent (),
				Collections.<MediaRec>emptyList (),
				Optional.<String>absent (),
				Optional.<String>absent ());

		transaction.commit ();

		requestContext.addNotice (
			"Message " + message.getId () + " inserted");

		return null;

	}

}
