package wbs.sms.message.stats.console;

import java.io.PrintWriter;
import java.util.Map;

import org.joda.time.LocalDate;

import wbs.sms.message.stats.model.MessageStats;

public
interface SmsStatsTimeScheme {

	DateRange dateRange (
			LocalDate date);

	void goTableHeader (
			PrintWriter out,
			LocalDate start);

	MessageStats[] getData (
			LocalDate start,
			Map<LocalDate,MessageStats> groupStats);

	boolean[] getHilites (
			LocalDate start);

}
