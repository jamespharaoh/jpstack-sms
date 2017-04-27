package wbs.sms.route.test.console;

import static wbs.utils.collection.CollectionUtils.emptyList;
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

@PrototypeComponent ("routeTestInAction")
public
class RouteTestInAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

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
			"routeTestInResponder");

	}

	// implementation

	@Override
	public
	Responder goReal (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"goReal");

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					taskLogger,
					"RouteTestInAction.goReal ()",
					this);

		) {

			RouteRec route =
				routeHelper.findFromContextRequired ();

			MessageRec message =
				smsInboxLogic.inboxInsert (
					taskLogger,
					optionalAbsent (),
					textHelper.findOrCreate (
						taskLogger,
						requestContext.parameterRequired (
							"message")),
					smsNumberHelper.findOrCreate (
						taskLogger,
						requestContext.parameterRequired (
							"num_from")),
					requestContext.parameterRequired (
						"num_to"),
					route,
					optionalAbsent (),
					optionalAbsent (),
					emptyList (),
					optionalAbsent (),
					optionalAbsent ());

			transaction.commit ();

			requestContext.addNotice (
				"Message " + message.getId () + " inserted");

			return null;

		}

	}

}
