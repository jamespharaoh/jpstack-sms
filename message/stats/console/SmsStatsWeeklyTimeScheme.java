package wbs.sms.message.stats.console;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import javax.inject.Inject;

import org.joda.time.LocalDate;

import wbs.framework.application.annotations.SingletonComponent;
import wbs.framework.utils.etc.Html;
import wbs.sms.message.stats.logic.MessageStatsLogic;
import wbs.sms.message.stats.model.MessageStatsData;

@SingletonComponent ("smsStatsWeeklyTimeScheme")
public
class SmsStatsWeeklyTimeScheme
	implements SmsStatsTimeScheme {

	// dependencies

	@Inject
	MessageStatsLogic messageStatsLogic;

	// constants

	final static
	SimpleDateFormat monthNameLongFormat =
		new SimpleDateFormat ("MMMMM");

	final static
	SimpleDateFormat monthNameShortFormat =
		new SimpleDateFormat ("MMM");

	final static
	SimpleDateFormat weekDateFormat =
		new SimpleDateFormat ("EEE d");

	// implementation

	@Override
	public
	DateRange dateRange (
			LocalDate date) {

		// create calendar

		Calendar calendar =
			Calendar.getInstance ();

		calendar.setTime (date.toDate ());

		// work out start date

		if (calendar.get (Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {

			calendar.add (
				Calendar.DATE,
				- 28);

		} else {

			calendar.set (
				Calendar.DAY_OF_WEEK,
				Calendar.MONDAY);

			calendar.add (
				Calendar.DATE,
				- 29);

		}

		Date start =
			calendar.getTime ();

		// work out end date

		calendar.add (
			Calendar.DATE,
			63);

		Date end =
			calendar.getTime ();

		// return

		return new DateRange (
			start,
			end);

	}

	@Override
	public
	void goTableHeader (
			PrintWriter out,
			LocalDate start) {

		Calendar calendar =
			Calendar.getInstance ();

		// work out this week

		if (calendar.get (Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {

			calendar.add (Calendar.DATE, -1);

		} else {

			calendar.set (Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			calendar.add (Calendar.DATE, -2);

		}

		Date thisWeek =
			calendar.getTime ();

		out.println ("<tr> <th rowspan=\"3\">Type</th>");

		int lastMonth = -1;
		int cols = 0;
		int thisMonth = -1;

		String lastMonthNameLong = "";
		String lastMonthNameShort = "";

		for (int week = 0; week < 9; week++) {

			calendar.setTime (start.toDate ());
			calendar.add (Calendar.DATE, week * 7);

			thisMonth =
				calendar.get (Calendar.MONTH);

			if (thisMonth == lastMonth) {

				cols ++;

			} else {

				if (cols > 0) {

					out.println("<th colspan=\""
							+ cols
							+ "\">"
							+ Html.encode(cols > 1 ? lastMonthNameLong
									: lastMonthNameShort) + "</th>");

				}

				lastMonth = thisMonth;

				lastMonthNameLong =
					monthNameLongFormat.format (
						calendar.getTime ());

				lastMonthNameShort =
					monthNameShortFormat.format (
						calendar.getTime ());

				cols = 1;

			}

		}

		out.println("<th colspan=\""
				+ cols
				+ "\">"
				+ Html
						.encode(cols > 1 ? lastMonthNameLong
								: lastMonthNameShort) + "</th>");

		out.println("<tr>");

		for (int week = 0; week < 9; week++) {

			calendar.setTime (start.toDate ());
			calendar.add (Calendar.DATE, week * 7);

			out.println (
				(calendar.getTime ().equals (thisWeek)
					? "<th class=\"hilite\">"
					: "<th>")
				+ Html.encode (weekDateFormat.format (calendar.getTime ()))
				+ "</th>");

		}

		out.println ("</tr>");
		out.println ("<tr>");

		for (int week = 0; week < 9; week++) {

			calendar.setTime(start.toDate ());
			calendar.add(Calendar.DATE, week * 7 + 6);

			out.println("<th>"
					+ Html.encode(weekDateFormat.format(calendar.getTime()))
					+ "</th>");

		}

		out.println("</tr>");

	}

	@Override
	public
	MessageStatsData[] getData (
			LocalDate start,
			Map<LocalDate,MessageStatsData> groupStats) {

		Calendar calendar =
			Calendar.getInstance ();

		MessageStatsData[] data =
			new MessageStatsData [9];

		for (int week = 0; week < 9; week++) {

			MessageStatsData total =
				new MessageStatsData ();

			for (int day = 0; day < 7; day++) {

				calendar.clear ();
				calendar.setTime (start.toDate ());

				calendar.add (Calendar.DATE, week * 7 + day);

				MessageStatsData messageStats =
					groupStats.get (
						LocalDate.fromCalendarFields (calendar));

				if (messageStats != null) {

					messageStatsLogic.addTo (
						total,
						messageStats);

				}

			}

			data [week] = total;

		}

		return data;

	}

	@Override
	public
	boolean[] getHilites (
			LocalDate start) {

		Calendar cal =
			Calendar.getInstance ();

		boolean[] hilites =
			new boolean [9];

		for (int week = 0; week < 9; week++) {

			cal.setTime (start.toDate ());
			cal.add (Calendar.DATE, week * 7);

			hilites [week] =
				(cal.get (Calendar.MONTH) & 0x1) == 0x1;

		}

		return hilites;

	}

	public final static
	SmsStatsTimeScheme instance =
		new SmsStatsWeeklyTimeScheme ();

}
