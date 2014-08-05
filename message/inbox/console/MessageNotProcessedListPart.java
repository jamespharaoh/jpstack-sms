package wbs.sms.message.inbox.console;

import static wbs.framework.utils.etc.Misc.dateToInstant;
import static wbs.framework.utils.etc.Misc.stringFormat;

import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import org.joda.time.DateTimeZone;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.utils.etc.Html;
import wbs.platform.console.misc.TimeFormatter;
import wbs.platform.console.part.AbstractPagePart;
import wbs.sms.message.core.console.MessageConsoleHelper;
import wbs.sms.message.core.model.MessageRec;

@PrototypeComponent ("messageNotProcessedListPart")
public
class MessageNotProcessedListPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	MessageConsoleHelper messageHelper;

	@Inject
	TimeFormatter timeFormatter;

	// state

	List<MessageRec> messages;

	// implementation

	@Override
	public
	void prepare () {

		messages =
			messageHelper.findNotProcessed ();

	}

	@Override
	public
	void goBodyStuff () {

		printFormat (
			"<table class=\"list\">\n");

		printFormat (
			"<tr>\n",
			"<th>Time</th>\n",
			"<th>From</th>\n",
			"<th>To</th>\n",
			"<th>AV</th>\n",
			"<th style=\"text-align: left\">Message</th>\n",
			"<th>Notes</th>\n",
			"</tr>\n");

		int dayNumber = 0;

		Calendar calendar =
			Calendar.getInstance ();

		if (messages.size () == 0) {

			printFormat (
				"<tr>\n",
				"<td colspan=\"6\">(no messages)</td>\n",
				"</tr>\n");

		} else {

			for (MessageRec message
					: messages) {

				calendar.setTime (
					message.getCreatedTime ());

				int newDayNumber =
					+ (calendar.get (Calendar.YEAR) << 9)
					+ calendar.get (Calendar.DAY_OF_YEAR);

				if (newDayNumber != dayNumber) {

					printFormat (
						"<tr class=\"sep\">\n",

						"<tr style=\"font-weight: bold\">\n",

						"<td colspan=\"6\">%h</td>\n",
						timeFormatter.instantToDateStringLong (
							DateTimeZone.getDefault (),
							dateToInstant (
								message.getCreatedTime ())),

						"</tr>\n");

					dayNumber =
						newDayNumber;

				}

				printFormat (
					"%s\n",
					Html.magicTr (
						requestContext.resolveLocalUrl (
							stringFormat (
								"/messageNotProcessed.summary",
								"?messageId=%u",
								message.getId ())),
						false));

				printFormat (
					"<td>%h</td>\n",
					timeFormatter.instantToTimeString (
						DateTimeZone.getDefault (),
						dateToInstant (
							message.getCreatedTime ())));

				printFormat (
					"<td>%h</td>\n",
					message.getNumFrom ());

				printFormat (
					"<td>%h</td>\n",
					message.getNumTo ());

				printFormat (
					"<td>%h</td>\n",
					message.getAdultVerified ());

				printFormat (
					"<td>%h</td>\n",
					message.getText ().getText ());

				printFormat (
					"<td>%h</td>\n",
					message.getNotes ());

				printFormat (
					"</tr>\n");

			}

		}

		printFormat (
			"</table>\n");

	}

}
