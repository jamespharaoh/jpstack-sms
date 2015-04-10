package wbs.sms.route.test.console;

import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Cleanup;
import lombok.extern.log4j.Log4j;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.framework.record.GlobalId;
import wbs.framework.web.Responder;
import wbs.platform.console.action.ConsoleAction;
import wbs.platform.console.helper.ConsoleObjectManager;
import wbs.platform.console.param.ParamChecker;
import wbs.platform.console.param.ParamCheckerSet;
import wbs.platform.console.param.RegexpParamChecker;
import wbs.platform.console.request.ConsoleRequestContext;
import wbs.platform.service.model.ServiceRec;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.outbox.logic.MessageSender;
import wbs.sms.number.core.model.NumberObjectHelper;
import wbs.sms.number.core.model.NumberRec;
import wbs.sms.route.core.console.RouteConsoleHelper;
import wbs.sms.route.core.model.RouteRec;

import com.google.common.collect.ImmutableMap;

@Log4j
@PrototypeComponent ("routeTestOutAction")
public
class RouteTestOutAction
	extends ConsoleAction {

	@Inject
	ConsoleObjectManager objectManager;

	@Inject
	ConsoleRequestContext requestContext;

	@Inject
	Database database;

	@Inject
	NumberObjectHelper numberHelper;

	@Inject
	RouteConsoleHelper routeHelper;

	@Inject
	Provider<MessageSender> messageSender;

	@Override
	public
	Responder backupResponder () {
		return responder ("routeTestOutResponder");
	}

	@Override
	public
	Responder goReal () {

		MessageRec message = null;

		@Cleanup
		Transaction transaction =
			database.beginReadWrite (
				this);

		int routeId =
			requestContext.stuffInt ("routeId");

		RouteRec route =
			routeHelper.find (routeId);

		// check params

		Map<String,Object> params =
			paramsChecker.apply (requestContext);

		if (params == null) {
			throw new RuntimeException ();
		}

		// get params

		NumberRec number =
			numberHelper.findOrCreate (
				(String) params.get ("num_to"));

		ServiceRec testService =
			objectManager.findChildByCode (
				ServiceRec.class,
				GlobalId.root,
				"test");

		if (testService == null) {

			log.fatal (
				"Service system/test not found");

			requestContext.addError (
				"System configuration error");

			return null;

		}

		message =
			messageSender.get ()

			.number (
				number)

			.messageString (
				requestContext.parameter ("message"))

			.numFrom (
				requestContext.parameter ("num_from"))

			.route (
				route)

			.service (
				testService)

			.send ();

		transaction.commit ();

		if (message != null) {

			requestContext.addNotice (
				stringFormat (
					"Message %s inserted",
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
