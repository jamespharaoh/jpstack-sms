package wbs.sms.route.tester.daemon;

import static wbs.framework.utils.etc.Misc.instantToDate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.service.model.ServiceRec;
import wbs.sms.command.model.CommandRec;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.logic.InboxLogic;
import wbs.sms.message.inbox.model.InboxAttemptRec;
import wbs.sms.message.inbox.model.InboxRec;
import wbs.sms.route.tester.model.RouteTestObjectHelper;
import wbs.sms.route.tester.model.RouteTestRec;

import com.google.common.base.Optional;

@Accessors (fluent = true)
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

	// properties

	@Getter @Setter
	InboxRec inbox;

	@Getter @Setter
	CommandRec command;

	@Getter @Setter
	Optional<Integer> commandRef;

	@Getter @Setter
	String rest;

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
	InboxAttemptRec handle () {

		Transaction transaction =
			database.currentTransaction ();

		MessageRec message =
			inbox.getMessage ();

		Matcher matcher =
			routeTestPattern.matcher (
				message.getText ().getText ());

		if (! matcher.find ()) {

			return inboxLogic.inboxNotProcessed (
				message,
				Optional.<ServiceRec>absent (),
				Optional.<AffiliateRec>absent (),
				Optional.of (command),
				"No route test info found in message body");

		}

		int routeTestId =
			Integer.parseInt (matcher.group (1));

		RouteTestRec routeTest =
			routeTestHelper.find (
				routeTestId);

		if (routeTest == null) {

			return inboxLogic.inboxNotProcessed (
				message,
				Optional.<ServiceRec>absent (),
				Optional.<AffiliateRec>absent (),
				Optional.of (command),
				"Response to unknown route test id");

		}

		message.setThreadId (
			routeTest.getSentMessage ().getThreadId ());

		if (routeTest.getReturnedTime () != null) {

			return inboxLogic.inboxNotProcessed (
				message,
				Optional.<ServiceRec>absent (),
				Optional.<AffiliateRec>absent (),
				Optional.of (command),
				"Duplicate response for route test");

		}

		routeTest

			.setReturnedTime (
				instantToDate (
					transaction.now ()))

			.setReturnedMessage (
				message);

		return inboxLogic.inboxProcessed (
			message,
			Optional.<ServiceRec>absent (),
			Optional.<AffiliateRec>absent (),
			command);

	}

	final static
	Pattern routeTestPattern =
		Pattern.compile ("ROUTETEST ID=(\\d+)");

}