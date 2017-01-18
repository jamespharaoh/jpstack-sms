package wbs.sms.message.stats.console;

import java.util.Map;

import org.joda.time.LocalDate;

import wbs.sms.message.stats.model.MessageStatsData;

import wbs.utils.string.FormatWriter;

public
interface SmsStatsTimeScheme {

	DateRange dateRange (
			LocalDate date);

	void goTableHeader (
			FormatWriter formatWriter,
			LocalDate start);

	MessageStatsData[] getData (
			LocalDate start,
			Map<LocalDate,MessageStatsData> groupStats);

	Boolean[] getHilites (
			LocalDate start);

	boolean groupByDate ();
	boolean groupByMonth ();

}
