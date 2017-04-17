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
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

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
			@NonNull TaskLogger parentTaskLogger) {

		MessageRec smsMessage =
			smsMessageHelper.findFromContextRequired ();

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

	@Override
	public
	void writePreamble (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull FormatWriter formatWriter,
			@NonNull Boolean submit) {

		MessageRec smsMessage =
			smsMessageHelper.findFromContextRequired ();

		htmlParagraphWriteFormat (
			"This outbound message is in the \"%h\" ",
			smsMessage.getStatus ().getDescription (),
			"state, and can be manually delivered.");

	}

	@Override
	public
	Optional <Responder> processFormSubmission (
			@NonNull TaskLogger parentTaskLogger,
			@NonNull Transaction transaction,
			@NonNull Object formState) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"processFormSubmission");

		// load data

		MessageRec smsMessage =
			smsMessageHelper.findFromContextRequired ();

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
			taskLogger,
			smsMessage,
			MessageStatus.manuallyDelivered);

		eventLogic.createEvent (
			taskLogger,
			"message_manually_delivered",
			userConsoleLogic.userRequired (),
			smsMessage);

		// commit and return

		transaction.commit ();

		requestContext.addNotice (
			"Message manually undelivered");

		return optionalAbsent ();

	}

}
