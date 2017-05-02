package wbs.sms.route.tester.daemon;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOf;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

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

	// singleton dependencies

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MessageObjectHelper messageHelper;

	@SingletonDependency
	RouteTestObjectHelper routeTestHelper;

	@SingletonDependency
	SmsInboxLogic smsInboxLogic;

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
	InboxAttemptRec handle (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"handle");

		) {

			MessageRec message =
				inbox.getMessage ();

			Matcher matcher =
				routeTestPattern.matcher (
					message.getText ().getText ());

			if (! matcher.find ()) {

				return smsInboxLogic.inboxNotProcessed (
					transaction,
					inbox,
					optionalAbsent (),
					optionalAbsent (),
					optionalOf (
						command),
					"No route test info found in message body");

			}

			Long routeTestId =
				Long.parseLong (
					matcher.group (1));

			Optional <RouteTestRec> routeTestOptional =
				routeTestHelper.find (
					transaction,
					routeTestId);

			if (
				optionalIsNotPresent (
					routeTestOptional)
			) {

				return smsInboxLogic.inboxNotProcessed (
					transaction,
					inbox,
					optionalAbsent (),
					optionalAbsent (),
					optionalOf (
						command),
					"Response to unknown route test id");

			}

			RouteTestRec routeTest =
				routeTestOptional.get ();

			message.setThreadId (
				routeTest.getSentMessage ().getThreadId ());

			if (routeTest.getReturnedTime () != null) {

				return smsInboxLogic.inboxNotProcessed (
					transaction,
					inbox,
					optionalAbsent (),
					optionalAbsent (),
					optionalOf (
						command),
					"Duplicate response for route test");

			}

			routeTest

				.setReturnedTime (
					transaction.now ())

				.setReturnedMessage (
					message);

			return smsInboxLogic.inboxProcessed (
				transaction,
				inbox,
				optionalAbsent (),
				optionalAbsent (),
				command);

		}

	}

	final static
	Pattern routeTestPattern =
		Pattern.compile (
			"ROUTETEST ID=(\\d+)");

}