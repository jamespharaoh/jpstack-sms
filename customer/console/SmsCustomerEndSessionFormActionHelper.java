package wbs.sms.customer.console;

import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWriteFormat;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.formaction.ConsoleFormActionHelper;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Transaction;
import wbs.framework.logging.TaskLogger;

import wbs.platform.user.console.UserConsoleLogic;

import wbs.sms.customer.logic.SmsCustomerLogic;
import wbs.sms.customer.model.SmsCustomerRec;
import wbs.sms.customer.model.SmsCustomerSessionRec;

import wbs.utils.string.FormatWriter;

import wbs.web.responder.Responder;

@SingletonComponent ("smsCustomerEndSessionFormActionHelper")
public
class SmsCustomerEndSessionFormActionHelper
	implements ConsoleFormActionHelper <SmsCustomerEndSessionForm> {

	// singleton dependencies

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	SmsCustomerConsoleHelper smsCustomerHelper;

	@SingletonDependency
	SmsCustomerLogic smsCustomerLogic;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// public implementation

	@Override
	public
	Boolean canBePerformed () {

		SmsCustomerRec customer =
			smsCustomerHelper.findFromContextRequired ();

		return isNotNull (
			customer.getActiveSession ());

	}

	@Override
	public
	void writePreamble (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormatWriter formatWriter) {

		SmsCustomerRec customer =
			smsCustomerHelper.findFromContextRequired ();

		SmsCustomerSessionRec session =
			customer.getActiveSession ();

		htmlParagraphWriteFormat (
			formatWriter,
			"This customer has an active session since %s. ",
			userConsoleLogic.timestampWithoutTimezoneString (
				session.getStartTime ()),
			"This action will end the customer's session.");
	
	}

	@Override
	public
	Optional <Responder> processFormSubmission (
			@NonNull Transaction transaction,
			@NonNull SmsCustomerEndSessionForm formState) {

		SmsCustomerRec customer =
			smsCustomerHelper.findFromContextRequired ();

		if (
			isNull (
				customer.getActiveSession ())
		) {

			requestContext.addError (
				"Customer has no active session");

		} else {

			smsCustomerLogic.sessionEndManually (
				userConsoleLogic.userRequired (),
				customer,
				formState.reason ());

			transaction.commit ();

			requestContext.addNotice (
				"Customer session ended");

		}

		return optionalAbsent ();

	}

}
