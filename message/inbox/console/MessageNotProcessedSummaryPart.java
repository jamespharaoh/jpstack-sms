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
import wbs.console.priv.UserPrivChecker;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.platform.user.console.UserConsoleLogic;

import wbs.sms.message.core.console.MessageConsoleHelper;
import wbs.sms.message.core.console.MessageConsoleLogic;
import wbs.sms.message.core.model.MessageRec;
import wbs.sms.message.core.model.MessageStatus;

import wbs.utils.string.FormatWriter;

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
	UserPrivChecker privChecker;

	@SingletonDependency
	UserConsoleLogic userConsoleLogic;

	// state

	MessageRec message;

	// implementation

	@Override
	public
	void prepare (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepare");

		) {

			message =
				messageHelper.findFromContextRequired (
					transaction);

		}

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContent");

		) {

			if (
				enumNotEqualSafe (
					message.getStatus (),
					MessageStatus.notProcessed)
			) {

				formatWriter.writeFormat (
					"<p>Message is not in correct state</p>");

				return;

			}

			htmlTableOpenDetails (
				formatWriter);

			htmlTableDetailsRowWrite (
				formatWriter,
				"ID",
				integerToDecimalString (
					message.getId ()));

			htmlTableDetailsRowWrite (
				formatWriter,
				"From",
				message.getNumFrom ());

			htmlTableDetailsRowWrite (
				formatWriter,
				"To",
				message.getNumTo ());

			htmlTableDetailsRowWrite (
				formatWriter,
				"Message",
				message.getText ().getText ());

			htmlTableDetailsRowWriteRaw (
				formatWriter,
				"Route",
				() -> objectManager.writeTdForObjectMiniLink (
					transaction,
					formatWriter,
					privChecker,
					message.getRoute ()));

			htmlTableDetailsRowWriteRaw (
				formatWriter,
				"Status",
				() -> messageConsoleLogic.writeTdForMessageStatus (
					transaction,
					formatWriter,
					message.getStatus ()));

			htmlTableDetailsRowWriteHtml (
				formatWriter,
				"Time sent",
				ifNotNullThenElseEmDash (
					message.getNetworkTime (),
					() -> userConsoleLogic.timestampWithTimezoneString (
						transaction,
						message.getNetworkTime ())));

			htmlTableDetailsRowWrite (
				formatWriter,
				"Time received",
				userConsoleLogic.timestampWithTimezoneString (
					transaction,
					message.getCreatedTime ()));

			htmlTableDetailsRowWrite (
				formatWriter,
				"Charge",
				integerToDecimalString (
					message.getCharge ()));

			htmlTableDetailsRowWrite (
				formatWriter,
				"AV status",
				ifNullThenEmDash (
					message.getAdultVerified ()));

			htmlTableClose (
				formatWriter);

		}

	}

}
