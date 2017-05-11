package wbs.sms.route.test.console;

import static wbs.utils.collection.CollectionUtils.emptyList;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;

import lombok.NonNull;

import wbs.console.action.ConsoleAction;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.text.model.TextObjectHelper;

import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.inbox.logic.SmsInboxLogic;
import wbs.sms.number.core.model.NumberObjectHelper;
import wbs.sms.route.core.console.RouteConsoleHelper;
import wbs.sms.route.core.model.RouteRec;

import wbs.web.responder.Responder;

@PrototypeComponent ("routeTestTwoWayAction")
public
class RouteTestTwoWayAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	RouteConsoleHelper routeHelper;

	@SingletonDependency
	SmsInboxLogic smsInboxLogic;

	@SingletonDependency
	NumberObjectHelper smsNumberHelper;

	@SingletonDependency
	TextObjectHelper textHelper;

	// details

	@Override
	public
	Responder backupResponder (
			@NonNull TaskLogger parentTaskLogger) {

		return responder (
			"routeTestTwoWayResponder");

	}

	// implementation

	@Override
	public
	Responder goReal (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWriteWithoutParameters (
					logContext,
					parentTaskLogger,
					"goReal");

		) {

			RouteRec route =
				routeHelper.findFromContextRequired (
					transaction);

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
					transaction,
					optionalAbsent (),
					textHelper.findOrCreate (
						transaction,
						messageString),
					smsNumberHelper.findOrCreate (
						transaction,
						numFrom),
					numTo,
					route,
					optionalAbsent (),
					optionalAbsent (),
					emptyList (),
					optionalAbsent (),
					optionalAbsent ());

			requestContext.addNoticeFormat (
				"Message %s inserted",
				integerToDecimalString (
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

}
