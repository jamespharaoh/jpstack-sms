package wbs.sms.message.core.console;

import static wbs.utils.etc.EnumUtils.enumEqualSafe;
import static wbs.utils.etc.EnumUtils.enumInSafe;
import static wbs.utils.etc.EnumUtils.enumNotInSafe;
import static wbs.utils.etc.Misc.isNotNull;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
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

import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;
import wbs.sms.message.outbox.console.OutboxConsoleHelper;
import wbs.sms.message.outbox.logic.SmsOutboxLogic;
import wbs.sms.message.outbox.model.OutboxRec;

import wbs.utils.string.FormatWriter;

import wbs.web.responder.Responder;

@PrototypeComponent ("messageManuallyRetryFormActionHelper")
public
class MessageManuallyRetryFormActionHelper
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
	OutboxConsoleHelper smsOutboxHelper;

	@SingletonDependency
	SmsOutboxLogic smsOutboxLogic;

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

			Optional <OutboxRec> smsOutboxOptional =
				smsOutboxHelper.find (
					transaction,
					smsMessage.getId ());

			boolean show = (

				enumEqualSafe (
					smsMessage.getDirection (),
					MessageDirection.out)

				&& enumInSafe (
					smsMessage.getStatus (),
					MessageStatus.pending,
					MessageStatus.failed,
					MessageStatus.cancelled,
					MessageStatus.blacklisted)

			);

			boolean submit = (

				enumInSafe (
					smsMessage.getStatus (),
					MessageStatus.failed,
					MessageStatus.cancelled,
					MessageStatus.blacklisted)

			) || (

				optionalIsPresent (
					smsOutboxOptional)

				&& isNull (
					smsOutboxOptional.get ().getSending ())

			);

			return new Permissions ()
				.canView (show)
				.canPerform (submit);

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

			if (submit) {

				htmlParagraphWriteFormat (
					"This outbound message is in the \"%h\" ",
					smsMessage.getStatus ().getDescription (),
					"state, and can be manually retried.");

			} else {

				htmlParagraphWriteFormat (
					"This outbound message is in the \"%h\" ",
					smsMessage.getStatus ().getDescription (),
					"state, but is currently being sent, so no action can be ",
					"taken at this time.");

			}

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

			Optional <OutboxRec> smsOutboxOptional =
				smsOutboxHelper.find (
					transaction,
					smsMessage.getId ());

			// check state

			if (
				enumNotInSafe (
					smsMessage.getStatus (),
					MessageStatus.pending,
					MessageStatus.failed,
					MessageStatus.cancelled,
					MessageStatus.blacklisted)
			) {

				requestContext.addError (
					"Message in invalid state for this operation");

				return optionalAbsent ();

			}

			if (

				optionalIsPresent (
					smsOutboxOptional)

				&& isNotNull (
					smsOutboxOptional.get ().getSending ())

			) {

				requestContext.addError (
					"Message is currently being sent");

				return optionalAbsent ();

			}

			// retry message

			smsOutboxLogic.retryMessage (
				transaction,
				smsMessage);

			eventLogic.createEvent (
				transaction,
				"message_manually_retried",
				userConsoleLogic.userRequired (
					transaction),
				smsMessage);

			transaction.commit ();

			requestContext.addNotice (
				"Message manually retried");

			return optionalAbsent ();

		}

	}

}
