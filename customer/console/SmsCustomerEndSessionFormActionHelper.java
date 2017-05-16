package wbs.sms.customer.console;

import static wbs.utils.etc.NullUtils.isNotNull;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.NullUtils.isNull;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWriteFormat;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.formaction.ConsoleFormActionHelper;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.SingletonComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.platform.user.console.UserConsoleLogic;

import wbs.sms.customer.logic.SmsCustomerLogic;
import wbs.sms.customer.model.SmsCustomerRec;
import wbs.sms.customer.model.SmsCustomerSessionRec;

import wbs.utils.string.FormatWriter;

import wbs.web.responder.Responder;

@SingletonComponent ("smsCustomerEndSessionFormActionHelper")
public
class SmsCustomerEndSessionFormActionHelper
	implements ConsoleFormActionHelper <SmsCustomerEndSessionForm, Object> {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

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
	Permissions canBePerformed (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"canBePerformed");

		) {

			SmsCustomerRec customer =
				smsCustomerHelper.findFromContextRequired (
					transaction);

			boolean show = (
				isNotNull (
					customer.getActiveSession ())
			);

			return new Permissions ()
				.canView (show)
				.canPerform (show);

		}

	}

	@Override
	public
	void writePreamble (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter,
			@NonNull Boolean submit) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"writePreamble");

		) {

			SmsCustomerRec customer =
				smsCustomerHelper.findFromContextRequired (
					transaction);

			SmsCustomerSessionRec session =
				customer.getActiveSession ();

			htmlParagraphWriteFormat (
				formatWriter,
				"This customer has an active session since %s. ",
				userConsoleLogic.timestampWithoutTimezoneString (
					transaction,
					session.getStartTime ()),
				"This action will end the customer's session.");

		}

	}

	@Override
	public
	Optional <Responder> processFormSubmission (
			@NonNull Transaction parentTransaction,
			@NonNull SmsCustomerEndSessionForm formState) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"processFormSubmission");

		) {

			SmsCustomerRec customer =
				smsCustomerHelper.findFromContextRequired (
					transaction);

			if (
				isNull (
					customer.getActiveSession ())
			) {

				requestContext.addError (
					"Customer has no active session");

			} else {

				smsCustomerLogic.sessionEndManually (
					transaction,
					userConsoleLogic.userRequired (
						transaction),
					customer,
					formState.reason ());

				transaction.commit ();

				requestContext.addNotice (
					"Customer session ended");

			}

			return optionalAbsent ();

		}

	}

}
