package wbs.sms.message.stats.console;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.joda.time.LocalDate;

import wbs.framework.utils.etc.Html;
import wbs.sms.message.stats.model.MessageStatsData;

public
class SmsStatsDailyTimeScheme
		implements SmsStatsTimeScheme {

	private final static
	SimpleDateFormat monthNameLongFormat =
		new SimpleDateFormat ("MMMMM");

	private final static
	SimpleDateFormat monthNameShortFormat =
		new SimpleDateFormat ("MMM");

	private final static
	SimpleDateFormat weekDateFormat =
		new SimpleDateFormat ("EEE d");

	private
	SmsStatsDailyTimeScheme () {
	}

	@Override
	public
	DateRange dateRange (
			LocalDate date) {

		// create calendar

		Calendar cal =
			Calendar.getInstance ();

		cal.setTime (
			date.toDate ());

		// work out start date

		if (cal.get (Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {

			cal.add (Calendar.DATE, -1);

		} else {

			cal.set (Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			cal.add (Calendar.DATE, -2);

		}

		Date start =
			cal.getTime ();

		// work out end date

		cal.add (Calendar.DATE, 9);

		Date end =
			cal.getTime ();

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

		// create calendar

		Calendar cal =
			Calendar.getInstance ();

		// work out today

		cal.clear (Calendar.HOUR_OF_DAY);
		cal.clear (Calendar.MINUTE);
		cal.clear (Calendar.SECOND);
		cal.clear (Calendar.MILLISECOND);

		Date today =
			cal.getTime ();

		// do months row

		out.println("<tr> <th rowspan=\"2\">Type</th>");

		int lastMonth = -1, cols = 0, thisMonth = -1;
		String lastMonthNameLong = "", lastMonthNameShort = "";
		for (int day = 0; day < 9; day++) {

			cal.setTime (
				start.toDate ());

			cal.add(Calendar.DATE, day);
			thisMonth = cal.get(Calendar.MONTH);
			if (thisMonth == lastMonth)
				cols++;
			else {
				if (cols > 0) {
					out.println("<th colspan=\""
							+ cols
							+ "\">"
							+ Html.encode(cols > 1 ? lastMonthNameLong
									: lastMonthNameShort) + "</th>");
				}
				lastMonth = thisMonth;
				lastMonthNameLong = monthNameLongFormat.format(cal.getTime());
				lastMonthNameShort = monthNameShortFormat.format(cal.getTime());
				cols = 1;
			}
		}
		out.println("<th colspan=\""
				+ cols
				+ "\">"
				+ Html
						.encode(cols > 1 ? lastMonthNameLong
								: lastMonthNameShort) + "</th>");
		out.println("</tr>");

		// do dayss row
		out.println("<tr>");
		for (int day = 0; day < 9; day++) {

			cal.setTime (
				start.toDate ());

			cal.add (Calendar.DATE, day);

			out.println((cal.getTime().equals(today) ? "<th class=\"hilite\">"
					: "<th>")
					+ Html.encode(weekDateFormat.format(cal.getTime()))
					+ "</th>");

		}

		out.println ("</tr>");

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

		for (int day = 0; day < 9; day++) {

			calendar.clear ();

			calendar.setTime (start.toDate ());

			calendar.add (Calendar.DATE, day);

			data [day] =
				groupStats.get (
					LocalDate.fromCalendarFields (calendar));

		}

		return data;

	}

	private final static
	boolean[] hilites = {
		true,
		true,
		false,
		false,
		false,
		false,
		false,
		true,
		true
	};

	@Override
	public
	boolean[] getHilites (
			LocalDate start) {

		return hilites;

	}

	public final static
	SmsStatsTimeScheme instance =
		new SmsStatsDailyTimeScheme ();

}
