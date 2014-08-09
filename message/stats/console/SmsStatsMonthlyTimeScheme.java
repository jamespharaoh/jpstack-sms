package wbs.sms.message.stats.console;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.joda.time.LocalDate;

import wbs.framework.utils.etc.Html;
import wbs.sms.message.stats.model.MessageStats;

public
class SmsStatsMonthlyTimeScheme
	implements SmsStatsTimeScheme {

	private final static
	SimpleDateFormat monthNameShortFormat =
		new SimpleDateFormat ("MMM");

	@Override
	public
	DateRange dateRange (
			LocalDate date) {

		// create calendar

		Calendar calendar =
			Calendar.getInstance ();

		calendar.setTime (date.toDate ());

		// work out start date

		calendar.set (Calendar.DATE, 1);
		calendar.add (Calendar.MONTH, -4);

		Date start = calendar.getTime ();

		// work out end date

		calendar.add (Calendar.MONTH, 9);

		Date end = calendar.getTime ();

		// return

		return new DateRange (start, end);

	}

	@Override
	public
	void goTableHeader (
			PrintWriter out,
			LocalDate start) {

		Calendar calendar =
			Calendar.getInstance ();

		// work out current month

		calendar.clear (Calendar.HOUR_OF_DAY);
		calendar.clear (Calendar.MINUTE);
		calendar.clear (Calendar.SECOND);
		calendar.clear (Calendar.MILLISECOND);
		calendar.clear (Calendar.DATE);

		Date thisMonth = calendar.getTime ();

		out.println ("<tr>");


		out.println ("<th rowspan=\"2\">Type</th>");

		int lastYear = -1;
		int thisYear = -1;
		int cols = 0;

		for (int month = 0; month < 9; month++) {

			calendar.setTime (start.toDate ());

			calendar.add (Calendar.MONTH, month);

			thisYear =
				calendar.get (Calendar.YEAR);

			if (thisYear == lastYear) {

				cols++;

			} else {

				if (cols > 0) {

					out.println("<th colspan=\"" + cols + "\">" + lastYear
							+ "</th>");

				}

				lastYear = thisYear;

				cols = 1;

			}

		}

		out.println ("<th colspan=\"" + cols + "\">" + lastYear + "</th>");

		out.println ("</tr>");

		out.println ("<tr>");

		for (int month = 0; month < 9; month++) {

			calendar.setTime (start.toDate ());

			calendar.add (Calendar.MONTH, month);

			out.println (""
				+ (calendar.getTime ().equals (thisMonth)
					? "<th class=\"hilite\">"
					: "<th>")
				+ Html.encode (
					monthNameShortFormat.format (
						calendar.getTime ()))
				+ "</th>");

		}

		out.println ("</tr>");

	}

	@Override
	public
	MessageStats[] getData (
			LocalDate start,
			Map<LocalDate,MessageStats> groupStats) {

		Calendar calendar =
			Calendar.getInstance ();

		MessageStats[] data =
			new MessageStats [9];

		for (
			int month = 0;
			month < 9;
			month ++
		) {

			MessageStats total =
				new MessageStats ();

			calendar.clear ();

			calendar.setTime (start.toDate ());

			calendar.add (Calendar.MONTH, month);

			int currentMonth =
				calendar.get (Calendar.MONTH);

			do {

				MessageStats messageStats =
					groupStats.get (
						LocalDate.fromCalendarFields (calendar));

				if (messageStats != null)
					total.plusEq (messageStats);

				calendar.add (Calendar.DATE, 1);

			} while (calendar.get (Calendar.MONTH) == currentMonth);

			data [month] = total;

		}

		return data;

	}

	@Override
	public
	boolean[] getHilites (
			LocalDate start) {

		Calendar cal =
			Calendar.getInstance ();

		boolean[] hilites = new boolean[9];

		for (int month = 0; month < 9; month++) {

			cal.clear ();
			cal.setTime (start.toDate ());
			cal.add (Calendar.MONTH, month);

			hilites [month] =
				cal.get (Calendar.MONTH) >= 6;

		}

		return hilites;

	}

	public final static
	SmsStatsTimeScheme instance =
		new SmsStatsMonthlyTimeScheme ();

}
