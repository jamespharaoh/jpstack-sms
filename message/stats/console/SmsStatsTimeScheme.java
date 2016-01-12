package wbs.sms.message.stats.console;

import java.io.PrintWriter;
import java.util.Map;

import org.joda.time.LocalDate;

import wbs.sms.message.stats.model.MessageStatsData;

public
interface SmsStatsTimeScheme {

	DateRange dateRange (
			LocalDate date);

	void goTableHeader (
			PrintWriter out,
			LocalDate start);

	MessageStatsData[] getData (
			LocalDate start,
			Map<LocalDate,MessageStatsData> groupStats);

	boolean[] getHilites (
			LocalDate start);

	boolean groupByDate ();
	boolean groupByMonth ();

}
