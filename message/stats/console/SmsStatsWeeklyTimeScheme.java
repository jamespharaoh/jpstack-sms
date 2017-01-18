package wbs.sms.message.stats.console;

import static wbs.utils.etc.LogicUtils.equalWithClass;
import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.NumberUtils.moreThanOne;
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
import wbs.framework.component.annotations.SingletonDependency;

import wbs.sms.message.stats.logic.MessageStatsLogic;
import wbs.sms.message.stats.model.MessageStatsData;

import wbs.utils.string.FormatWriter;

@SingletonComponent ("smsStatsWeeklyTimeScheme")
public
class SmsStatsWeeklyTimeScheme
	implements SmsStatsTimeScheme {

	// singleton dependencies

	@SingletonDependency
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
			@NonNull FormatWriter formatWriter,
			@NonNull LocalDate start) {

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

		htmlTableRowOpen (
			formatWriter);

		htmlTableHeaderCellWrite (
			formatWriter,
			"Type",
			htmlRowSpanAttribute (
				3l));

		long lastMonth = -1l;
		long cols = 0l;
		long thisMonth = -1l;

		String lastMonthNameLong = "";
		String lastMonthNameShort = "";

		for (
			long week = 0l;
			week < 9l;
			week ++
		) {

			calendar.setTime (
				start.toDate ());

			calendar.add (
				Calendar.DATE,
				toJavaIntegerRequired (
					week * 7));

			thisMonth =
				calendar.get (
					Calendar.MONTH);

			if (thisMonth == lastMonth) {

				cols ++;

			} else {

				String lastMonthNameLongTemp =
					lastMonthNameLong;

				String lastMonthNameShortTemp =
					lastMonthNameShort;

				if (cols > 0) {

					htmlTableHeaderCellWrite (
						formatWriter,
						ifThenElse (
							cols > 1,
							() -> lastMonthNameLongTemp,
							() -> lastMonthNameShortTemp),
						htmlColumnSpanAttribute (
							cols));

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

		String lastMonthNameLongTemp =
			lastMonthNameLong;

		String lastMonthNameShortTemp =
			lastMonthNameShort;

		htmlTableHeaderCellWrite (
			formatWriter,
			ifThenElse (
				moreThanOne (
					cols),
				() -> lastMonthNameLongTemp,
				() -> lastMonthNameShortTemp),
			htmlColumnSpanAttribute (
				cols));

		htmlTableRowClose ();

		htmlTableRowOpen ();

		for (
			long week = 0l;
			week < 9l;
			week ++
		) {

			calendar.setTime (
				start.toDate ());

			calendar.add (
				Calendar.DATE,
				toJavaIntegerRequired (
					week * 7));

			if (
				equalWithClass (
					Date.class,
					calendar.getTime (),
					thisWeek)
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

		htmlTableRowOpen (
			formatWriter);

		for (
			long week = 0l;
			week < 9l;
			week ++
		) {

			calendar.setTime (
				start.toDate ());

			calendar.add (
				Calendar.DATE,
				toJavaIntegerRequired (
					week * 7 + 6));

			htmlTableHeaderCellWrite (
				formatWriter,
				weekDateFormat.format (
					calendar.getTime ()));

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

		for (int week = 0; week < 9; week++) {

			MessageStatsData total =
				new MessageStatsData ();

			for (int day = 0; day < 7; day++) {

				calendar.clear ();
				calendar.setTime (start.toDate ());

				calendar.add (Calendar.DATE, week * 7 + day);

				MessageStatsData messageStats =
					groupStats.get (
						LocalDate.fromCalendarFields (
							calendar));

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
	Boolean[] getHilites (
			@NonNull LocalDate start) {

		Calendar calendar =
			Calendar.getInstance ();

		Boolean[] hilites =
			new Boolean [9];

		for (
			int week = 0;
			week < 9;
			week ++
		) {

			calendar.setTime (
				start.toDate ());

			calendar.add (
				Calendar.DATE,
				week * 7);

			hilites [week] =
				(calendar.get (Calendar.MONTH) & 0x1) == 0x1;

		}

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
