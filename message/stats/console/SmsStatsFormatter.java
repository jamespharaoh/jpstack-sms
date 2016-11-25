package wbs.sms.message.stats.console;

import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.string.StringUtils.stringFormat;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.LocalDate;

import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;

import wbs.platform.currency.logic.CurrencyLogic;

import wbs.sms.message.stats.console.GroupedStatsSource.GroupStats;
import wbs.sms.message.stats.model.MessageStatsData;
import wbs.sms.route.core.model.RouteRec;

import wbs.utils.etc.PropertyUtils;

import wbs.web.utils.HtmlUtils;

/**
 * Responsible for outputing standardised tables of message statistics. Requires
 * a SmsStatsSource class which is responsible for retrieving the data, and is then
 * invoked with a date and a timescheme, which conspire to work out which stats
 * to retrieve and how to aggregate them by date. Really quite complicated
 * unless you look at the results and the various client classes.
 */
@Accessors (fluent = true)
@PrototypeComponent ("smsStatsFormatter")
public
class SmsStatsFormatter {

	// singleton dependencies

	@SingletonDependency
	CurrencyLogic currencyLogic;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	// properties

	@Getter @Setter
	GroupedStatsSource groupedStatsSource;

	@Getter @Setter
	LocalDate mainDate;

	@Getter @Setter
	SmsStatsTimeScheme timeScheme;

	// state

	PrintWriter out;

	LocalDate startDate;
	LocalDate endDate;
	Map<String,GroupStats> stats;

	// implementation

	public
	void go () {

		setupDates ();

		loadStats ();

		goOutput ();

	}

	void setupDates () {

		DateRange dateRange =
			timeScheme.dateRange (mainDate);

		startDate =
			LocalDate.fromDateFields (
				dateRange.getStart ());

		endDate =
			LocalDate.fromDateFields (
				dateRange.getEnd ());

	}

	void loadStats () {

		stats =
			groupedStatsSource.load (
				timeScheme,
				startDate,
				endDate);

	}

	static
	enum RowDirection {
		in,
		out;
	};

	@Accessors (fluent = true)
	static
	class Row {

		@Getter @Setter
		RowDirection direction;

		@Getter @Setter
		String name;

		@Getter @Setter
		String className;

		@Getter @Setter
		String fieldName;

		public
		long getData (
				MessageStatsData messageStats) {

			return (Long)
				PropertyUtils.propertyGetAuto (
					messageStats,
					fieldName);

		}

	}

	static
	List<Row> rows =
		new ArrayList<Row> ();

	static {

		rows.add (
			new Row ()
				.name ("In total")
				.className ("plain")
				.direction (RowDirection.in)
				.fieldName ("inTotal"));

		rows.add (
			new Row ()
				.name ("Out total")
				.className ("plain")
				.direction (RowDirection.out)
				.fieldName ("outTotal"));

		rows.add (
			new Row ()
				.name ("Out held")
				.className ("unknown")
				.direction (RowDirection.out)
				.fieldName ("outHeld"));

		rows.add (
			new Row ()
				.name ("Out pending")
				.className ("unknown")
				.direction (RowDirection.out)
				.fieldName ("outPending"));

		rows.add (
			new Row ()
				.name ("Out sent")
				.className ("unknown")
				.direction (RowDirection.out)
				.fieldName ("outSent"));

		rows.add (
			new Row ()
				.name ("Out submitted")
				.className ("unknown")
				.direction (RowDirection.out)
				.fieldName ("outSubmitted"));

		rows.add (
			new Row ()
				.name ("Out delivered")
				.className ("succeeded")
				.direction (RowDirection.out)
				.fieldName ("outDelivered"));

		rows.add (
			new Row ()
				.name ("Out undelivered")
				.className ("failed")
				.direction (RowDirection.out)
				.fieldName ("outUndelivered"));

		rows.add (
			new Row ()
				.name ("Out cancelled")
				.className ("failed")
				.direction (RowDirection.out)
				.fieldName ("outCancelled"));

		rows.add (
			new Row ()
				.name ("Out failed")
				.className ("failed")
				.direction (RowDirection.out)
				.fieldName ("outFailed"));

		rows.add (
			new Row ()
				.name ("Out no report")
				.className ("failed")
				.direction (RowDirection.out)
				.fieldName ("outReportTimedOut"));

		rows.add (
			new Row ()
				.name ("Out blacklisted")
				.className ("failed")
				.direction (RowDirection.out)
				.fieldName ("outBlacklisted"));

		rows.add (
			new Row ()
				.name ("Out manually undelivered")
				.className ("failed")
				.direction (RowDirection.out)
				.fieldName ("outManuallyUndelivered"));

	}

	protected
	void goTotalsHeader (
			int colSpan) {

		out.print (
			stringFormat (
				"<tr>\n",
				"<td class=\"group-name\" colspan=\"%h\">TOTALS</td>",
				integerToDecimalString (
					colSpan),
				"</tr>\n"));

	}

	protected
	void goSectionHeader (
			String group,
			String url,
			int colSpan) {

		if (url != null) {

			out.println("<tr>");

			out.print("<td");
			out.print(" class=\"group-name\"");
			out.print(" colspan=\"" + colSpan + "\"");
			out.print(" style=\"cursor: pointer;\"");
			out.print(" onmouseover=\"this.className='group-name-hover'\"");
			out.print(" onmouseout=\"this.className='group-name'\"");
			out.print(" onclick=\"window.location='" + HtmlUtils.htmlJavascriptEncode(url) + "'\"");
			out.println(">");

			out.println(HtmlUtils.htmlEncode(group));

			out.println("</td>");

			out.println("</tr>");
		} else {
			out.println("<tr> <td class=\"group-name\" colspan=\"" + colSpan
					+ "\">" + HtmlUtils.htmlEncode(group) + "</td> </tr>");
		}

	}

	protected
	void goSectionBody (
			RouteRec route,
			MessageStatsData[] data,
			boolean[] hilites) {

		for (Row row : rows) {

			StringBuilder stringBuilder =
				new StringBuilder ();

			stringBuilder.append (
				stringFormat (
					"<tr>\n",

					"<td",
					" class=\"%h\"",
					row.className,
					">%h</td>\n",
					row.name));

			boolean foundSomething =
				false;

			for (
				int index = 0;
				index < data.length;
				index ++
			) {

				MessageStatsData messageStatus =
					data [index];

				String className =
					hilites [index]
						? "hi-" + row.className
						: row.className;

				String charge = "";

				if (messageStatus == null) {

					charge = "";

				} else {

					long messageCount =
						row.getData (
							messageStatus);

					if (messageCount > 0) {
						foundSomething = true;
					}

					switch (row.direction) {

					case in:

						charge =
							route != null && route.getInCharge () > 0
								? currencyLogic.formatText (
									route.getCurrency (),
									route.getInCharge () * messageCount)
								: noZero (
									messageCount);

						break;

					case out:

						charge =
							route != null && route.getOutCharge () > 0
								? currencyLogic.formatText (
									route.getCurrency (),
									Long.valueOf(route.getOutCharge () * messageCount))
								: noZero (messageCount);

						break;

					}

				}

				stringBuilder.append (
					stringFormat (
						"<td",
						" class=\"%h\"",
						className,
						" style=\"text-align: right\"",
						">%h</td>\n",
						charge));

			}

			stringBuilder.append (
				stringFormat (
					"</tr>\n"));

			if (foundSomething)
				out.print (stringBuilder.toString ());

		}

	}

	void goOutput () {

		out =
			requestContext.writer ();

		out.print (
			stringFormat (
				"<table class=\"list\">\n"));

		timeScheme.goTableHeader (
			out,
			startDate);

		boolean[] hilites =
			timeScheme.getHilites (startDate);

		// goTotals (job, out, hilites);

		for (Map.Entry<String,GroupedStatsSource.GroupStats> entry
				: stats.entrySet ()) {

			GroupedStatsSource.GroupStats groupStats =
				entry.getValue ();

			String group =
				entry.getKey ();

			MessageStatsData[] data =
				timeScheme.getData (
					startDate,
					groupStats.getStatsByDate ());

			out.print (
				stringFormat (
					"<tr class=\"sep\">\n"));

			goSectionHeader (
				group,
				groupStats.getUrl (),
				data.length + 1);

			goSectionBody (
				entry.getValue ().getRoute (),
				data,
				hilites);

		}

		out.print (
			stringFormat (
				"</table>\n"));

	}

	String noZero (
			int num) {

		return num == 0
			? ""
			: Integer.toString (
				num);

	}

	String noZero (
			long num) {

		return num == 0
			? ""
			: Long.toString (
				num);

	}

}
