package wbs.sms.route.tester.daemon;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.daemon.ReceivedMessage;
import wbs.sms.route.tester.model.RouteTestObjectHelper;
import wbs.sms.route.tester.model.RouteTestRec;

@Log4j
@SingletonComponent ("routeTesterCommand")
public
class RouteTesterCommand
	implements CommandHandler {

	@Inject
	Database database;

	@Inject
	MessageObjectHelper messageHelper;

	@Inject
	RouteTestObjectHelper routeTestHelper;

	@Override
	public
	String[] getCommandTypes () {

		return new String [] {
			"root.route_tester_response"
		};

	}

	@Override
	public
	Status handle (
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

			log.error (
				"No route test found in message: " + message.getId ());

			return Status.notprocessed;

		}

		int routeTestId =
			Integer.parseInt (matcher.group (1));

		RouteTestRec routeTest =
			routeTestHelper.find (
				routeTestId);

		if (routeTest == null) {

			log.error (
				"Response for unknown route test!?: " + routeTestId);

			return Status.notprocessed;

		}

		message.setThreadId (
			routeTest.getSentMessage ().getThreadId ());

		if (routeTest.getReturnedTime () != null) {

			log.error (
				"Duplicate response for route test " + routeTestId);

			return Status.processed;

		}

		routeTest
			.setReturnedTime (new Date ())
			.setReturnedMessage (message);

		transaction.commit ();

		return Status.processed;

	}

	final static
	Pattern routeTestPattern =
		Pattern.compile ("ROUTETEST ID=(\\d+)");

}