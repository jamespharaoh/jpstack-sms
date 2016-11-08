package wbs.sms.route.test.console;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringFormat;

import java.util.Map;

import javax.inject.Provider;

import com.google.common.collect.ImmutableMap;

import lombok.Cleanup;
import lombok.NonNull;

import wbs.console.action.ConsoleAction;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.param.ParamChecker;
import wbs.console.param.ParamCheckerSet;
import wbs.console.param.RegexpParamChecker;
import wbs.console.request.ConsoleRequestContext;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.entity.record.GlobalId;
import wbs.framework.logging.TaskLogger;
import wbs.framework.web.Responder;
import wbs.platform.service.console.ServiceConsoleHelper;
import wbs.platform.service.model.ServiceRec;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.logic.SmsMessageSender;
import wbs.sms.number.core.model.NumberObjectHelper;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.route.core.console.RouteConsoleHelper;
import wbs.sms.route.core.model.RouteRec;

@PrototypeComponent ("routeTestOutAction")
public
class RouteTestOutAction
	extends ConsoleAction {

	// singleton dependencies

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	Database database;

	@SingletonDependency
	NumberObjectHelper numberHelper;

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
	Responder backupResponder () {

		return responder (
			"routeTestOutResponder");

	}

	// implementation

	@Override
	public
	Responder goReal (
			@NonNull TaskLogger taskLogger) {

		MessageRec message = null;

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				"RouteTestOutAction.goReal ()",
				this);

		RouteRec route =
			routeHelper.findRequired (
				requestContext.stuffInteger (
					"routeId"));

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
				(String) params.get ("num_to"));

		ServiceRec testService =
			serviceHelper.findByCodeRequired (
				GlobalId.root,
				"test");

		message =
			messageSender.get ()

			.number (
				number)

			.messageString (
				requestContext.parameterRequired (
					"message"))

			.numFrom (
				requestContext.parameterRequired (
					"num_from"))

			.route (
				route)

			.service (
				testService)

			.send ();

		transaction.commit ();

		if (message != null) {

			requestContext.addNoticeFormat (
				"Message %s inserted",
				integerToDecimalString (
					message.getId ()));

		}

		return null;

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
