package wbs.sms.route.test.console;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;

import java.util.Map;

import javax.inject.Provider;

import com.google.common.collect.ImmutableMap;

import lombok.NonNull;

import wbs.console.action.ConsoleAction;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.param.ParamChecker;
import wbs.console.param.ParamCheckerSet;
import wbs.console.param.RegexpParamChecker;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.OwnedTransaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.service.console.ServiceConsoleHelper;
import wbs.platform.service.model.ServiceRec;

import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.logic.SmsMessageSender;
import wbs.sms.number.core.model.NumberObjectHelper;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.route.core.console.RouteConsoleHelper;
import wbs.sms.route.core.model.RouteRec;

import wbs.web.responder.Responder;

@PrototypeComponent ("routeTestOutAction")
public
class RouteTestOutAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	Database database;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	NumberObjectHelper numberHelper;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	RouteConsoleHelper routeHelper;

	@SingletonDependency
	ServiceConsoleHelper serviceHelper;

	// prototype dependencies

	@PrototypeDependency
	Provider <SmsMessageSender> messageSender;

	// dependencies

	@Override
	public
	Responder backupResponder (
			@NonNull TaskLogger parentTaskLogger) {

		return responder (
			"routeTestOutResponder");

	}

	// implementation

	@Override
	public
	Responder goReal (
			@NonNull TaskLogger parentTaskLogger) {

		try (

			OwnedTransaction transaction =
				database.beginReadWrite (
					logContext,
					parentTaskLogger,
					"goReal");

		) {

			RouteRec route =
				routeHelper.findFromContextRequired (
					transaction);

			// check params

			Map<String,Object> params =
				paramsChecker.apply (
					requestContext);

			if (params == null) {
				throw new RuntimeException ();
			}

			// get params

			NumberRec number =
				numberHelper.findOrCreate (
					transaction,
					(String) params.get ("num_to"));

			ServiceRec testService =
				serviceHelper.findByCodeRequired (
					transaction,
					GlobalId.root,
					"test");

			MessageRec message =
				messageSender.get ()

				.number (
					number)

				.messageString (
					transaction,
					requestContext.parameterRequired (
						"message"))

				.numFrom (
					requestContext.parameterRequired (
						"num_from"))

				.route (
					route)

				.service (
					testService)

				.send (
					transaction);

			transaction.commit ();

			if (message != null) {

				requestContext.addNoticeFormat (
					"Message %s inserted",
					integerToDecimalString (
						message.getId ()));

			}

			return null;

		}

	}

	static
	ParamCheckerSet paramsChecker =
		new ParamCheckerSet (
			new ImmutableMap.Builder<String,ParamChecker<?>> ()

				.put (
					"num_to",
					new RegexpParamChecker (
						"Please enter a valid destination number",
						"\\d+"))

				.build ());

}
