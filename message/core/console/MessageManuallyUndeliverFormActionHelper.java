package wbs.sms.message.core.console;

import static wbs.utils.etc.EnumUtils.enumEqualSafe;
import static wbs.utils.etc.EnumUtils.enumInSafe;
import static wbs.utils.etc.EnumUtils.enumNotEqualSafe;
import static wbs.utils.etc.EnumUtils.enumNotInSafe;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphWriteFormat;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.apache.commons.lang3.tuple.Pair;

import wbs.console.formaction.ConsoleFormActionHelper;
import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Transaction;
import wbs.framework.logging.TaskLogger;

import wbs.platform.event.logic.EventLogic;
import wbs.platform.user.console.UserConsoleLogic;

import wbs.sms.message.core.logic.SmsMessageLogic;
import wbs.sms.message.core.model.MessageDirection;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;

import wbs.utils.string.FormatWriter;

import wbs.web.responder.Responder;

@PrototypeComponent ("messageManuallyUndeliverFormActionHelper")
public
class MessageManuallyUndeliverFormActionHelper
	implements ConsoleFormActionHelper <Object> {

	// singleton dependencies

	@SingletonDependency
	EventLogic eventLogic;

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
	Pair <Boolean, Boolean> canBePerformed () {

		MessageRec smsMessage =
			smsMessageHelper.findFromContextRequired ();

		boolean show = (

			enumEqualSafe (
				smsMessage.getDirection (),
				MessageDirection.out)

			&& enumInSafe (
				smsMessage.getStatus (),
				MessageStatus.sent,
				MessageStatus.submitted,
				MessageStatus.delivered,
				MessageStatus.manuallyDelivered)

		);

		return Pair.of (
			show,
			show);

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
			"state, and can be manually undelivered.");

	}

	@Override
	public
	Optional <Responder> processFormSubmission (
			@NonNull Transaction transaction,
			@NonNull Object formState) {

		// load data

		MessageRec smsMessage =
			smsMessageHelper.findFromContextRequired ();

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
				MessageStatus.sent,
				MessageStatus.submitted,
				MessageStatus.delivered)
		) {

			requestContext.addError (
				"Message in invalid state for this operation");

			return optionalAbsent ();

		}

		smsMessageLogic.messageStatus (
			smsMessage,
			MessageStatus.manuallyUndelivered);

		eventLogic.createEvent (
			"message_manually_undelivered",
			userConsoleLogic.userRequired (),
			smsMessage);

		transaction.commit ();

		requestContext.addNotice (
			"Message manually undelivered");

		return optionalAbsent ();

	}

}
