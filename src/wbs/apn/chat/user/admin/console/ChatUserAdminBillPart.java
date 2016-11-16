package wbs.apn.chat.user.admin.console;

import static wbs.utils.etc.EnumUtils.enumEqualSafe;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.time.TimeUtils.millisToInstant;
import static wbs.utils.web.HtmlBlockUtils.htmlHeadingTwoWrite;
import static wbs.utils.web.HtmlFormUtils.htmlFormClose;
import static wbs.utils.web.HtmlFormUtils.htmlFormOpenPostAction;
import static wbs.utils.web.HtmlTableUtils.htmlTableCellWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableHeaderRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenList;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableRowOpen;

import java.util.List;

import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import wbs.apn.chat.bill.logic.ChatCreditLogic;
import wbs.apn.chat.bill.model.ChatUserBillLogObjectHelper;
import wbs.apn.chat.bill.model.ChatUserBillLogRec;
import wbs.apn.chat.core.model.ChatRec;
import wbs.apn.chat.user.core.console.ChatUserConsoleHelper;
import wbs.apn.chat.user.core.logic.ChatUserLogic;
import wbs.apn.chat.user.core.model.ChatUserRec;
import wbs.apn.chat.user.core.model.ChatUserType;
import wbs.console.helper.manager.ConsoleObjectManager;
import wbs.console.part.AbstractPagePart;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.Database;
import wbs.framework.database.Transaction;
import wbs.utils.time.TimeFormatter;

@PrototypeComponent ("chatUserAdminBillPart")
public
class ChatUserAdminBillPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	ChatCreditLogic chatCreditLogic;

	@SingletonDependency
	ChatUserBillLogObjectHelper chatUserBillLogHelper;

	@SingletonDependency
	ChatUserConsoleHelper chatUserHelper;

	@SingletonDependency
	ChatUserLogic chatUserLogic;

	@SingletonDependency
	ConsoleObjectManager consoleObjectManager;

	@SingletonDependency
	Database database;

	@SingletonDependency
	TimeFormatter timeFormatter;

	// state

	ChatUserRec chatUser;
	ChatRec chat;

	List <ChatUserBillLogRec> todayBillLogs;
	List <ChatUserBillLogRec> allBillLogs;
	boolean billLimitReached;

	// implementation

	@Override
	public
	void prepare () {

		Transaction transaction =
			database.currentTransaction ();

		chatUser =
			chatUserHelper.findRequired (
				requestContext.stuffInteger (
					"chatUserId"));

		if (
			enumEqualSafe (
				chatUser.getType (),
				ChatUserType.monitor)
		) {
			return;
		}

		DateTimeZone timezone =
			chatUserLogic.getTimezone (
				chatUser);

		LocalDate today =
			transaction
				.now ()
				.toDateTime (timezone)
				.toLocalDate ();

		Instant startTime =
			today
				.toDateTimeAtStartOfDay (timezone)
				.toInstant ();

		Instant endTime =
			today
				.plusDays (1)
				.toDateTimeAtStartOfDay (timezone)
				.toInstant ();

		todayBillLogs =
			chatUserBillLogHelper.findByTimestamp (
				chatUser,
				new Interval (
					startTime,
					endTime));

		allBillLogs =
			chatUserBillLogHelper.findByTimestamp (
				chatUser,
				new Interval (
					millisToInstant (0),
					endTime));

		billLimitReached =
			chatCreditLogic.userBillLimitApplies (
				chatUser);

	}

	@Override
	public
	void renderHtmlBodyContent () {

		if (
			enumEqualSafe (
				chatUser.getType (),
				ChatUserType.monitor)
		) {

			formatWriter.writeLineFormat (
				"<p>This is a monitor and cannot be billed.</p>");

			return;

		}

		boolean dailyAdminRebillLimitReached =
			todayBillLogs.size () >= 3;

		boolean canBypassDailyAdminRebillLimit =
			requestContext.canContext (
				"chat.manage");

		if (billLimitReached) {

			formatWriter.writeFormat (
				"<p>Daily billed message limit reached.</p>");

		}

		if (dailyAdminRebillLimitReached) {

			formatWriter.writeLineFormat (
				"<p>Daily admin rebill limit reached<br>");

			formatWriter.writeLineFormat (
				"%h admin rebills have been actioned today</p>",
				integerToDecimalString (
					todayBillLogs.size ()));

		}

		if (
			! billLimitReached
			&& (
				! dailyAdminRebillLimitReached
				|| canBypassDailyAdminRebillLimit
			)
		) {

			htmlFormOpenPostAction (
				requestContext.resolveLocalUrl (
					"/chatUser.admin.bill"));

			formatWriter.writeFormat (
				"<p><input",
				" type=\"submit\"",
				" value=\"reset billing\"",
				"></p>");

			htmlFormClose ();

		}

		htmlHeadingTwoWrite (
			"History");

		htmlTableOpenList ();

		htmlTableHeaderRowWrite (
			"Date",
			"Time",
			"User");

		for (
			ChatUserBillLogRec billLog
				: allBillLogs
		) {

			htmlTableRowOpen ();

			htmlTableCellWrite (
				timeFormatter.dateStringShort (
					chatUserLogic.getTimezone (
						chatUser),
					billLog.getTimestamp ()));

			htmlTableCellWrite (
				timeFormatter.timeString (
					chatUserLogic.getTimezone (
						chatUser),
					billLog.getTimestamp ()));

			consoleObjectManager.writeTdForObjectMiniLink (
				billLog.getUser ());

			htmlTableRowClose ();

		}

		htmlTableClose ();

	}

}