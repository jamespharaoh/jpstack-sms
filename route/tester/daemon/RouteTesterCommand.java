package wbs.sms.route.tester.daemon;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import lombok.Cleanup;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.daemon.ReceivedMessage;
import wbs.sms.message.inbox.logic.InboxLogic;
import wbs.sms.route.tester.model.RouteTestObjectHelper;
import wbs.sms.route.tester.model.RouteTestRec;

@SingletonComponent ("routeTesterCommand")
public
class RouteTesterCommand
	implements CommandHandler {

	// dependencies

	@Inject
	Database database;

	@Inject
	InboxLogic inboxLogic;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	RouteTestObjectHelper routeTestHelper;

	// details

	@Override
	public
	String[] getCommandTypes () {

		return new String [] {
			"root.route_tester_response"
		};

	}

	// implementation

	@Override
	public
	void handle (
			int commandId,
			ReceivedMessage receivedMessage) {

		@Cleanup
		Transaction transaction =
			database.beginReadWrite ();

		MessageRec message =
			messageHelper.find (
				receivedMessage.getMessageId());

		Matcher matcher =
			routeTestPattern.matcher (
				message.getText ().getText ());

		if (! matcher.find ()) {

			inboxLogic.inboxNotProcessed (
				message,
				null,
				null,
				null,
				"No route test info found in message body");

			transaction.commit ();

			return;

		}

		int routeTestId =
			Integer.parseInt (matcher.group (1));

		RouteTestRec routeTest =
			routeTestHelper.find (
				routeTestId);

		if (routeTest == null) {

			inboxLogic.inboxNotProcessed (
				message,
				null,
				null,
				null,
				"Response to unknown route test id");

			transaction.commit ();

			return;

		}

		message.setThreadId (
			routeTest.getSentMessage ().getThreadId ());

		if (routeTest.getReturnedTime () != null) {

			inboxLogic.inboxNotProcessed (
				message,
				null,
				null,
				null,
				"Duplicate response for route test");

			transaction.commit ();

			return;

		}

		routeTest
			.setReturnedTime (new Date ())
			.setReturnedMessage (message);

		inboxLogic.inboxProcessed (
			message,
			null,
			null,
			null);

		transaction.commit ();

	}

	final static
	Pattern routeTestPattern =
		Pattern.compile ("ROUTETEST ID=(\\d+)");

}