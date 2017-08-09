package wbs.sms.message.stats.console;

import static wbs.utils.etc.LogicUtils.equalWithClass;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.NumberUtils.moreThan;
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

@SingletonComponent ("smsStatsMonthlyTimeScheme")
public
class SmsStatsMonthlyTimeScheme
	implements SmsStatsTimeScheme {

	// singleton dependencies

	@SingletonDependency
	MessageStatsLogic messageStatsLogic;

	// constants

	private final static
	SimpleDateFormat monthNameShortFormat =
		new SimpleDateFormat ("MMM");

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
			@NonNull FormatWriter formatWriter,
			@NonNull LocalDate start) {

		Calendar calendar =
			Calendar.getInstance ();

		// work out current month

		calendar.clear (Calendar.HOUR_OF_DAY);
		calendar.clear (Calendar.MINUTE);
		calendar.clear (Calendar.SECOND);
		calendar.clear (Calendar.MILLISECOND);
		calendar.clear (Calendar.DATE);

		Date thisMonth = calendar.getTime ();

		htmlTableRowOpen (
			formatWriter);

		htmlTableHeaderCellWrite (
			formatWriter,
			"Type",
			htmlRowSpanAttribute (
				2l));

		long lastYear = -1l;
		long thisYear = -1l;
		long cols = 0l;

		for (
			long month = 0l;
			month < 9l;
			month ++
		) {

			calendar.setTime (
				start.toDate ());

			calendar.add (
				Calendar.MONTH,
				toJavaIntegerRequired (
					month));

			thisYear =
				calendar.get (
					Calendar.YEAR);

			if (thisYear == lastYear) {

				cols ++;

			} else {

				if (cols > 0l) {

					htmlTableHeaderCellWrite (
						formatWriter,
						integerToDecimalString (
							lastYear),
						htmlColumnSpanAttribute (
							cols));

				}

				lastYear = thisYear;

				cols = 1l;

			}

		}

		htmlTableHeaderCellWrite (
			formatWriter,
			integerToDecimalString (
				lastYear),
			htmlColumnSpanAttribute (
				cols));

		htmlTableRowClose (
			formatWriter);

		htmlTableRowOpen (
			formatWriter);

		for (
			long month = 0l;
			month < 9l;
			month ++
		) {

			calendar.setTime (
				start.toDate ());

			calendar.add (
				Calendar.MONTH,
				toJavaIntegerRequired (
					month));

			if (
				equalWithClass (
					Date.class,
					calendar.getTime (),
					thisMonth)
			) {

				htmlTableHeaderCellWrite (
					formatWriter,
					monthNameShortFormat.format (
						calendar.getTime ()),
					htmlClassAttribute (
						"hilite"));

			} else {


				htmlTableHeaderCellWrite (
					formatWriter,
					monthNameShortFormat.format (
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

		for (
			int month = 0;
			month < 9;
			month ++
		) {

			MessageStatsData total =
				new MessageStatsData ();

			calendar.clear ();

			calendar.setTime (
				start.toDate ());

			calendar.add (
				Calendar.MONTH,
				month);

			int currentMonth =
				calendar.get (
					Calendar.MONTH);

			do {

				MessageStatsData messageStats =
					groupStats.get (
						LocalDate.fromCalendarFields (
							calendar));

				if (messageStats != null) {

					messageStatsLogic.addTo (
						total,
						messageStats);

				}

				calendar.add (
					Calendar.DATE,
					1);

			} while (calendar.get (Calendar.MONTH) == currentMonth);

			data [month] = total;

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

		for (int month = 0; month < 9; month++) {

			calendar.clear ();

			calendar.setTime (
				start.toDate ());

			calendar.add (
				Calendar.MONTH,
				month);

			hilites [month] =
				moreThan (
					calendar.get (
						Calendar.MONTH),
					6);

		}

		return hilites;

	}

	@Override
	public
	boolean groupByDate () {
		return false;
	}

	@Override
	public
	boolean groupByMonth () {
		return true;
	}

	public final static
	SmsStatsTimeScheme instance =
		new SmsStatsMonthlyTimeScheme ();

}
