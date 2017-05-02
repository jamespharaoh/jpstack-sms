package wbs.sms.message.core.console;

import static wbs.utils.etc.EnumUtils.enumEqualSafe;
import static wbs.utils.etc.EnumUtils.enumInSafe;
import static wbs.utils.etc.EnumUtils.enumNotEqualSafe;
import static wbs.utils.etc.EnumUtils.enumNotInSafe;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWriteFormat;

import com.google.common.base.Optional;

import lombok.NonNull;

import wbs.console.formaction.ConsoleFormActionHelper;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.console.UserConsoleLogic;

import wbs.sms.message.core.logic.SmsMessageLogic;
import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;

import wbs.utils.string.FormatWriter;

import wbs.web.responder.Responder;

@PrototypeComponent ("messageManuallyDeliverFormActionHelper")
public
class MessageManuallyDeliverFormActionHelper
	implements ConsoleFormActionHelper <Object, Object> {

	// singleton dependencies

	@SingletonDependency
	EventLogic eventLogic;

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	MessageConsoleHelper smsMessageHelper;

	@SingletonDependency
	SmsMessageLogic smsMessageLogic;

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

			MessageRec smsMessage =
				smsMessageHelper.findFromContextRequired (
					transaction);

			boolean show = (

				enumEqualSafe (
					smsMessage.getDirection (),
					MessageDirection.out)

				&& enumInSafe (
					smsMessage.getStatus (),
					MessageStatus.undelivered,
					MessageStatus.manuallyUndelivered,
					MessageStatus.reportTimedOut)

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

			MessageRec smsMessage =
				smsMessageHelper.findFromContextRequired (
					transaction);

			htmlParagraphWriteFormat (
				"This outbound message is in the \"%h\" ",
				smsMessage.getStatus ().getDescription (),
				"state, and can be manually delivered.");

		}

	}

	@Override
	public
	Optional <Responder> processFormSubmission (
			@NonNull Transaction parentTransaction,
			@NonNull Object formState) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"processFormSubmission");

		) {

			// load data

			MessageRec smsMessage =
				smsMessageHelper.findFromContextRequired (
					transaction);

			// check state

			if (
				enumNotEqualSafe (
					smsMessage.getDirection (),
					MessageDirection.out)
			) {
				throw new RuntimeException ();
			}

			if (
				enumNotInSafe (
					smsMessage.getStatus (),
					MessageStatus.undelivered,
					MessageStatus.reportTimedOut,
					MessageStatus.manuallyUndelivered)
			) {

				requestContext.addError (
					"Message in invalid state for this operation");

				return optionalAbsent ();

			}

			// perform update

			smsMessageLogic.messageStatus (
				transaction,
				smsMessage,
				MessageStatus.manuallyDelivered);

			eventLogic.createEvent (
				transaction,
				"message_manually_delivered",
				userConsoleLogic.userRequired (
					transaction),
				smsMessage);

			// commit and return

			transaction.commit ();

			requestContext.addNotice (
				"Message manually undelivered");

			return optionalAbsent ();

		}

	}

}
