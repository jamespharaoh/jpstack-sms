package wbs.sms.message.inbox.console;

import static wbs.utils.etc.EnumUtils.enumNotEqualSafe;
import static wbs.utils.etc.LogicUtils.ifNotNullThenElseEmDash;
import static wbs.utils.etc.LogicUtils.ifNullThenEmDash;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWriteHtml;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWriteRaw;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;

import lombok.NonNull;

import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.logging.LogContext;
import wbs.framework.logging.TaskLogger;

import wbs.platform.user.console.UserConsoleLogic;

import wbs.sms.message.core.console.MessageConsoleHelper;
import wbs.sms.message.core.console.MessageConsoleLogic;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;

@PrototypeComponent ("messageNotProcessedSummaryPart")
public
class MessageNotProcessedSummaryPart
	extends AbstractPagePart {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	MessageConsoleLogic messageConsoleLogic;

	@SingletonDependency
	MessageConsoleHelper messageHelper;

	@SingletonDependency
	ConsoleObjectManager objectManager;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// state

	MessageRec message;

	// implementation

	@Override
	public
	void prepare (
			@NonNull TaskLogger parentTaskLogger) {

		message =
			messageHelper.findFromContextRequired ();

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull TaskLogger parentTaskLogger) {

		TaskLogger taskLogger =
			logContext.nestTaskLogger (
				parentTaskLogger,
				"renderHtmlBodyContent");

		if (
			enumNotEqualSafe (
				message.getStatus (),
				MessageStatus.notProcessed)
		) {

			formatWriter.writeFormat (
				"<p>Message is not in correct state</p>");

			return;

		}

		htmlTableOpenDetails ();

		htmlTableDetailsRowWrite (
			"ID",
			integerToDecimalString (
				message.getId ()));

		htmlTableDetailsRowWrite (
			"From",
			message.getNumFrom ());

		htmlTableDetailsRowWrite (
			"To",
			message.getNumTo ());

		htmlTableDetailsRowWrite (
			"Message",
			message.getText ().getText ());

		htmlTableDetailsRowWriteRaw (
			"Route",
			() -> objectManager.writeTdForObjectMiniLink (
				taskLogger,
				message.getRoute ()));

		htmlTableDetailsRowWriteRaw (
			"Status",
			() -> messageConsoleLogic.writeTdForMessageStatus (
				formatWriter,
				message.getStatus ()));

		htmlTableDetailsRowWriteHtml (
			"Time sent",
			ifNotNullThenElseEmDash (
				message.getNetworkTime (),
				() -> userConsoleLogic.timestampWithTimezoneString (
					message.getNetworkTime ())));

		htmlTableDetailsRowWrite (
			"Time received",
			userConsoleLogic.timestampWithTimezoneString (
				message.getCreatedTime ()));

		htmlTableDetailsRowWrite (
			"Charge",
			integerToDecimalString (
				message.getCharge ()));

		htmlTableDetailsRowWrite (
			"AV status",
			ifNullThenEmDash (
				message.getAdultVerified ()));

		htmlTableClose ();

	}

}
