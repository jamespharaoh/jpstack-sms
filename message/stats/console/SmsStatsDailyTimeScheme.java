package wbs.sms.message.stats.console;

import static wbs.utils.etc.LogicUtils.equalWithClass;
import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.NumberUtils.toJavaIntegerRequired;
import static wbs.web.utils.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlColumnSpanAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlRowSpanAttribute;
import static wbs.web.utils.HtmlTableUtils.htmlTableHeaderCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import lombok.NonNull;

import org.joda.time.LocalDate;

import wbs.framework.component.annotations.SingletonComponent;

import wbs.sms.message.stats.model.MessageStatsData;

import wbs.utils.string.FormatWriter;

@SingletonComponent ("smsStatsDailyTimeScheme")
public
class SmsStatsDailyTimeScheme
		implements SmsStatsTimeScheme {

	private final static
	SimpleDateFormat monthNameLongFormat =
		new SimpleDateFormat (
			"MMMMM");

	private final static
	SimpleDateFormat monthNameShortFormat =
		new SimpleDateFormat (
			"MMM");

	private final static
	SimpleDateFormat weekDateFormat =
		new SimpleDateFormat (
			"EEE d");

	@Override
	public
	DateRange dateRange (
			@NonNull LocalDate date) {

		// create calendar

		Calendar calendar =
			Calendar.getInstance ();

		calendar.setTime (
			date.toDate ());

		// work out start date

		if (calendar.get (Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {

			calendar.add (Calendar.DATE, -1);

		} else {

			calendar.set (Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			calendar.add (Calendar.DATE, -2);

		}

		Date start =
			calendar.getTime ();

		// work out end date

		calendar.add (Calendar.DATE, 9);

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
			@NonNull FormatWriter formatWriter,
			@NonNull LocalDate start) {

		// create calendar

		Calendar calendar =
			Calendar.getInstance ();

		// work out today

		calendar.clear (Calendar.HOUR_OF_DAY);
		calendar.clear (Calendar.MINUTE);
		calendar.clear (Calendar.SECOND);
		calendar.clear (Calendar.MILLISECOND);

		Date today =
			calendar.getTime ();

		// do months row

		htmlTableRowOpen (
			formatWriter);

		htmlTableHeaderCellWrite (
			formatWriter,
			"Type",
			htmlRowSpanAttribute (
				2l));

		long lastMonth = -1l;
		long cols = 0l;
		long thisMonth = -1l;

		String lastMonthNameLong = "";
		String lastMonthNameShort = "";

		for (
			long day = 0l;
			day < 9l;
			day ++
		) {

			calendar.setTime (
				start.toDate ());

			calendar.add (
				Calendar.DATE,
				toJavaIntegerRequired (
					day));

			thisMonth =
				calendar.get (
					Calendar.MONTH);

			if (thisMonth == lastMonth) {

				cols ++;

			} else {

				if (cols > 0) {

					String lastMonthNameLongTemp =
						lastMonthNameLong;

					String lastMonthNameShortTemp =
						lastMonthNameShort;

					htmlTableHeaderCellWrite (
						formatWriter,
						ifThenElse (
							cols > 1l,
							() -> lastMonthNameLongTemp,
							() -> lastMonthNameShortTemp),
						htmlColumnSpanAttribute (
							cols));

				}

				lastMonth =
					thisMonth;

				lastMonthNameLong =
					monthNameLongFormat.format (
						calendar.getTime ());

				lastMonthNameShort =
					monthNameShortFormat.format (
						calendar.getTime ());

				cols = 1;

			}

		}

		String lastMonthNameLongTemp =
			lastMonthNameLong;

		String lastMonthNameShortTemp =
			lastMonthNameShort;

		htmlTableHeaderCellWrite (
			formatWriter,
			ifThenElse (
				cols > 1,
				() -> lastMonthNameLongTemp,
				() -> lastMonthNameShortTemp),
			htmlColumnSpanAttribute (
				cols));

		htmlTableRowClose (
			formatWriter);

		// do dayss row

		htmlTableRowOpen (
			formatWriter);

		for (
			long day = 0l;
			day < 9l;
			day ++
		) {

			calendar.setTime (
				start.toDate ());

			calendar.add (
				Calendar.DATE,
				toJavaIntegerRequired (
					day));

			if (
				equalWithClass (
					Date.class,
					calendar.getTime (),
					today)
			) {

				htmlTableHeaderCellWrite (
					formatWriter,
					weekDateFormat.format (
						calendar.getTime ()),
					htmlClassAttribute (
						"hilite"));

			} else {

				htmlTableHeaderCellWrite (
					formatWriter,
					weekDateFormat.format (
						calendar.getTime ()));

			}

		}

		htmlTableRowClose (
			formatWriter);

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
	Boolean[] hilites = {
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
	Boolean[] getHilites (
			LocalDate start) {

		return hilites;

	}

	@Override
	public
	boolean groupByDate () {
		return true;
	}

	@Override
	public
	boolean groupByMonth () {
		return false;
	}

}
