package wbs.sms.route.tester.daemon;

import static wbs.framework.utils.etc.OptionalUtils.optionalIsNotPresent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.platform.affiliate.model.AffiliateRec;
import wbs.platform.service.model.ServiceRec;
import wbs.sms.command.model.CommandRec;
import wbs.sms.message.core.model.MessageObjectHelper;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.daemon.CommandHandler;
import wbs.sms.message.inbox.logic.SmsInboxLogic;
import wbs.sms.message.inbox.model.InboxAttemptRec;
import wbs.sms.message.inbox.model.InboxRec;
import wbs.sms.route.tester.model.RouteTestObjectHelper;
import wbs.sms.route.tester.model.RouteTestRec;

@Accessors (fluent = true)
@SingletonComponent ("routeTesterCommand")
public
class RouteTesterCommand
	implements CommandHandler {

	// dependencies

	@Inject
	Database database;

	@Inject
	SmsInboxLogic smsInboxLogic;

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
	Optional<Long> commandRef;

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

			return smsInboxLogic.inboxNotProcessed (
				inbox,
				Optional.<ServiceRec>absent (),
				Optional.<AffiliateRec>absent (),
				Optional.of (command),
				"No route test info found in message body");

		}

		Long routeTestId =
			Long.parseLong (
				matcher.group (1));

		Optional<RouteTestRec> routeTestOptional =
			routeTestHelper.find (
				routeTestId);

		if (
			optionalIsNotPresent (
				routeTestOptional)
		) {

			return smsInboxLogic.inboxNotProcessed (
				inbox,
				Optional.<ServiceRec>absent (),
				Optional.<AffiliateRec>absent (),
				Optional.of (command),
				"Response to unknown route test id");

		}

		RouteTestRec routeTest =
			routeTestOptional.get ();

		message.setThreadId (
			routeTest.getSentMessage ().getThreadId ());

		if (routeTest.getReturnedTime () != null) {

			return smsInboxLogic.inboxNotProcessed (
				inbox,
				Optional.<ServiceRec>absent (),
				Optional.<AffiliateRec>absent (),
				Optional.of (command),
				"Duplicate response for route test");

		}

		routeTest

			.setReturnedTime (
				transaction.now ())

			.setReturnedMessage (
				message);

		return smsInboxLogic.inboxProcessed (
			inbox,
			Optional.<ServiceRec>absent (),
			Optional.<AffiliateRec>absent (),
			command);

	}

	final static
	Pattern routeTestPattern =
		Pattern.compile ("ROUTETEST ID=(\\d+)");

}