package wbs.sms.message.stats.console;

import static wbs.utils.etc.LogicUtils.allOf;
import static wbs.utils.etc.LogicUtils.ifThenElse;
import static wbs.utils.etc.NumberUtils.fromJavaInteger;
import static wbs.utils.etc.NumberUtils.moreThanZero;
import static wbs.utils.etc.OptionalUtils.optionalIsPresent;
import static wbs.web.utils.HtmlAttributeUtils.htmlAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlAttributeFormat;
import static wbs.web.utils.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.web.utils.HtmlAttributeUtils.htmlColumnSpanAttribute;
import static wbs.web.utils.HtmlStyleUtils.htmlStyleRuleEntry;
import static wbs.web.utils.HtmlTableUtils.htmlTableCellWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenList;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowOpen;
import static wbs.web.utils.HtmlTableUtils.htmlTableRowSeparatorWrite;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.LocalDate;

import wbs.console.request.ConsoleRequestContext;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.platform.currency.logic.CurrencyLogic;

import wbs.sms.message.stats.console.GroupedStatsSource.GroupStats;
import wbs.sms.message.stats.model.MessageStatsData;
import wbs.sms.route.core.model.RouteRec;

import wbs.utils.etc.PropertyUtils;
import wbs.utils.string.FormatWriter;
import wbs.utils.string.LazyFormatWriter;

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

	@ClassSingletonDependency
	LogContext logContext;

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

	LocalDate startDate;
	LocalDate endDate;
	Map <String, GroupStats> stats;

	// implementation

	public
	void go (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"go");

		) {

			setupDates (
				transaction);

			loadStats (
				transaction);

			goOutput (
				formatWriter);

		}

	}

	private
	void setupDates (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"setupDates");

		) {

			DateRange dateRange =
				timeScheme.dateRange (mainDate);

			startDate =
				LocalDate.fromDateFields (
					dateRange.getStart ());

			endDate =
				LocalDate.fromDateFields (
					dateRange.getEnd ());

		}

	}

	private
	void loadStats (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"loadStats");

		) {

			stats =
				groupedStatsSource.load (
					transaction,
					timeScheme,
					startDate,
					endDate);

		}

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
			@NonNull FormatWriter formatWriter,
			@NonNull Long colSpan) {

		htmlTableRowOpen (
			formatWriter);

		htmlTableCellWrite (
			formatWriter,
			"TOTALS",
			htmlClassAttribute (
				"group-name"),
			htmlColumnSpanAttribute (
				colSpan));

		htmlTableRowClose (
			formatWriter);

	}

	protected
	void goSectionHeader (
			@NonNull FormatWriter formatWriter,
			@NonNull String group,
			@NonNull Optional <String> urlOptional,
			@NonNull Long colSpan) {

		htmlTableRowOpen (
			formatWriter);

		if (
			optionalIsPresent (
				urlOptional)
		) {

			htmlTableCellWrite (
				formatWriter,
				group,
				htmlClassAttribute (
					"group-name"),
				htmlColumnSpanAttribute (
					colSpan),
				htmlStyleRuleEntry (
					"cursor",
					"pointer"),
				htmlAttribute (
					"onmouseover",
					"this.className='group-name-hover'"),
				htmlAttribute (
					"onmouseout",
					"this.className='group-name'"),
				htmlAttributeFormat (
					"onclick",
					"window.location='%j'",
					urlOptional.get ()));

		} else {

			htmlTableCellWrite (
				formatWriter,
				group,
				htmlClassAttribute (
					"group-name"),
				htmlColumnSpanAttribute (
					colSpan));

		}

		htmlTableRowClose (
			formatWriter);

	}

	protected
	void goSectionBody (
			@NonNull FormatWriter formatWriter,
			@NonNull Optional <RouteRec> routeOptional,
			@NonNull MessageStatsData[] data,
			@NonNull Boolean[] hilites) {

		for (
			Row row
				: rows
		) {

			try (

				LazyFormatWriter buffer =
					new LazyFormatWriter ()

					.indentString (
						formatWriter.indentString ())

				;

			) {

				htmlTableRowOpen (
					buffer);

				htmlTableCellWrite (
					buffer,
					row.name,
					htmlClassAttribute (
						row.className));

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
								ifThenElse (
									allOf (
										() -> optionalIsPresent (
											routeOptional),
										() -> moreThanZero (
											routeOptional.get ().getInCharge ())),
									() -> currencyLogic.formatText (
										routeOptional.get ().getCurrency (),
										routeOptional.get ().getInCharge ()
											* messageCount),
									() -> noZero (
										messageCount));

							break;

						case out:

							charge =
								ifThenElse (
									allOf (
										() -> optionalIsPresent (
											routeOptional),
										() -> moreThanZero (
											routeOptional.get ().getOutCharge ())),
									() -> currencyLogic.formatText (
										routeOptional.get ().getCurrency (),
										routeOptional.get ().getOutCharge ()
											* messageCount),
									() -> noZero (
										messageCount));

							break;

						}

					}

					htmlTableCellWrite (
						buffer,
						charge,
						htmlClassAttribute (
							className),
						htmlStyleRuleEntry (
							"text-align",
							"right"));

				}

				htmlTableRowClose (
					buffer);

				if (foundSomething) {

					formatWriter.writeString (
						buffer.toString ());

				}

			}

		}

	}

	void goOutput (
			@NonNull FormatWriter formatWriter) {

		htmlTableOpenList (
			formatWriter);

		timeScheme.goTableHeader (
			formatWriter,
			startDate);

		Boolean[] hilites =
			timeScheme.getHilites (
				startDate);

		// goTotals (job, out, hilites);

		for (
			Map.Entry <String, GroupedStatsSource.GroupStats> entry
				: stats.entrySet ()
		) {

			GroupedStatsSource.GroupStats groupStats =
				entry.getValue ();

			String group =
				entry.getKey ();

			MessageStatsData[] data =
				timeScheme.getData (
					startDate,
					groupStats.getStatsByDate ());

			htmlTableRowSeparatorWrite (
				formatWriter);

			goSectionHeader (
				formatWriter,
				group,
				groupStats.getUrl (),
				fromJavaInteger (
					data.length + 1));

			goSectionBody (
				formatWriter,
				entry.getValue ().getRoute (),
				data,
				hilites);

		}

		htmlTableClose (
			formatWriter);

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
