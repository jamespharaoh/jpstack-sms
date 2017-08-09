package wbs.sms.message.stats.console;

import static wbs.utils.etc.Misc.toEnumOrNull;
import static wbs.utils.etc.NullUtils.isNull;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.web.utils.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphClose;
import static wbs.web.utils.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.web.utils.HtmlFormUtils.htmlFormClose;
import static wbs.web.utils.HtmlFormUtils.htmlFormOpenGetAction;
import static wbs.web.utils.HtmlTableUtils.htmlTableClose;
import static wbs.web.utils.HtmlTableUtils.htmlTableDetailsRowWrite;
import static wbs.web.utils.HtmlTableUtils.htmlTableOpenDetails;
import static wbs.web.utils.HtmlUtils.htmlLinkWrite;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.html.HtmlLink;
import wbs.console.html.ObsoleteDateField;
import wbs.console.part.AbstractPagePart;
import wbs.console.request.ConsoleRequestContext;
import wbs.console.tab.Tab;
import wbs.console.tab.TabList;

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.component.manager.ComponentProvider;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

import wbs.utils.string.FormatWriter;
import wbs.utils.time.core.DefaultTimeFormatter;

import wbs.web.misc.UrlParams;

@Accessors (fluent = true)
@PrototypeComponent ("genericMessageStatsPart")
public
class GenericMessageStatsPart
	extends AbstractPagePart {

	// singleton dependencies

	@ClassSingletonDependency
	LogContext logContext;

	@SingletonDependency
	ConsoleRequestContext requestContext;

	@SingletonDependency
	SmsStatsDailyTimeScheme smsStatsDailyTimeScheme;

	@SingletonDependency
	SmsStatsMonthlyTimeScheme smsStatsMonthlyTimeScheme;

	@SingletonDependency
	SmsStatsWeeklyTimeScheme smsStatsWeeklyTimeScheme;

	@SingletonDependency
	SmsStatsConsoleLogic statsConsoleLogic;

	@SingletonDependency
	DefaultTimeFormatter timeFormatter;

	// prototype dependencies

	@PrototypeDependency
	ComponentProvider <GroupedStatsSource> groupedStatsSourceProvider;

	@PrototypeDependency
	ComponentProvider <SmsStatsFormatter> statsFormatterProvider;

	@PrototypeDependency
	ComponentProvider <TabList> tabListProvider;

	// properties

	@Getter @Setter
	String url;

	@Getter @Setter
	SmsStatsSource statsSource;

	@Getter @Setter
	Set<SmsStatsCriteria> excludeCriteria =
		Collections.<SmsStatsCriteria>emptySet ();

	// state

	SmsStatsViewMode viewMode;

	TabList.Prepared viewTabsPrepared;
	TabList.Prepared splitTabsPrepared;

	Map <SmsStatsCriteria, Set <Long>> criteriaMap =
		new HashMap<> ();

	Map <String, String> criteriaInfo =
		new TreeMap<> ();

	SmsStatsCriteria splitCriteria;

	UrlParams urlParams =
		new UrlParams ();

	boolean ready;

	ObsoleteDateField dateField;

	// details

	@Override
	public
	Set <HtmlLink> links () {

		return ImmutableSet.<HtmlLink> of (

			HtmlLink.applicationCssStyle (
				"/style/sms-message-stats.css")

		);

	}

	// implementation

	public
	void prepareParams (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepareParams");

		) {

			// check split param

			splitCriteria =
				toEnumOrNull (
					SmsStatsCriteria.class,
					requestContext.parameterOrEmptyString (
						"split"));

			if (splitCriteria != null) {

				urlParams.add (
					"split",
					splitCriteria.toString ());

			}

			// check stats params

			for (
				SmsStatsCriteria crit
					: SmsStatsCriteria.values ()
			) {

				Optional<String> paramOptional =
					requestContext.parameter (
						crit.toString ());

				if (
					optionalIsNotPresent (
						paramOptional)
				) {
					continue;
				}

				String param =
					paramOptional.get ();

				Long critId =
					Long.parseLong (
						param);

				criteriaMap.put (
					crit,
					Collections.singleton (
						critId));

				criteriaInfo.put (
					crit.toString (),
					statsConsoleLogic.lookupGroupName (
						transaction,
						crit,
						critId));

				urlParams.set (
					crit.toString (),
					integerToDecimalString (
						critId));

			}

			// check view param

			viewMode =
				toEnumOrNull (
					SmsStatsViewMode.class,
					requestContext.parameterOrEmptyString (
						"view"));

			if (viewMode == null) {
				viewMode = SmsStatsViewMode.daily;
			}

			urlParams.set (
				"view",
				viewMode.toString ());

			// check date param

			dateField =
				ObsoleteDateField.parse (
					requestContext.parameterOrEmptyString (
						"date"));

			urlParams.set (
				"date",
				dateField.text);

		}

	}

	public
	void prepareTabs (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepareTabs");

		) {

			// prepare split tabs

			TabList splitTabs =
				tabListProvider.provide (
					transaction);

			Tab splitTab;

			splitTabs.add (
				splitTab = new TotalTab ());

			for (
				SmsStatsCriteria criteria
					: SmsStatsCriteria.values ()
			) {

				if (excludeCriteria.contains(criteria))
					continue;

				// if (critMap.containsKey (crit)) continue;

				Tab newTab =
					new StatsTab (criteria);

				splitTabs.add (
					newTab);

				if (criteria == splitCriteria)
					splitTab = newTab;

			}

			splitTabsPrepared =
				splitTabs.prepare (
					transaction,
					splitTab);

			// prepare view tabs

			TabList viewTabs =
				tabListProvider.provide (
					transaction);

			Tab viewTab = null;

			for (
				SmsStatsViewMode smsStatsViewMode
					: SmsStatsViewMode.values ()
			) {

				Tab newTab =
					new ViewTab (
						smsStatsViewMode.toString (),
						smsStatsViewMode.toString ());

				viewTabs.add (
					newTab);

				if (smsStatsViewMode == this.viewMode)
					viewTab = newTab;

			}

			viewTabsPrepared =
				viewTabs.prepare (
					transaction,
					viewTab);

		}

	}

	@Override
	public
	void prepare (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"prepare");

		) {

			// process page params

			prepareParams (
				transaction);

			// prepre tabs

			prepareTabs (
				transaction);

			// check inputs

			if (
				isNull (
					dateField.date)
			) {

				requestContext.addError (
					"Invalid date format");

				return;

			}

			ready = true;

		}

	}

	@Override
	public
	void renderHtmlBodyContent (
			@NonNull Transaction parentTransaction,
			@NonNull FormatWriter formatWriter) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"renderHtmlBodyContent");

		) {

			splitTabsPrepared.go (
				formatWriter);

			viewTabsPrepared.go (
				formatWriter);

			if (criteriaInfo.size () > 0) {

				htmlTableOpenDetails (
					formatWriter);

				for (
					Map.Entry <String, String> entry
						: criteriaInfo.entrySet ()
				) {

					String name =
						entry.getKey ();

					String value =
						entry.getValue ();

					htmlTableDetailsRowWrite (
						formatWriter,
						name,
						value);

				}

				htmlTableClose (
					formatWriter);

			}

			htmlFormOpenGetAction (
				formatWriter,
				url);

			UrlParams myUrlParams =
				new UrlParams (
					urlParams);

			urlParams.remove (
				"date");

			urlParams.printHidden (
				formatWriter);

			htmlParagraphOpen (
				formatWriter);

			formatWriter.writeLineFormat (
				"Date<br>");

			formatWriter.writeLineFormat (
				"<input",
				" type=\"text\"",
				" name=\"date\"",
				" value=\"%h\"",
				dateField.text,
				">");

			formatWriter.writeLineFormat (
				"<input",
				" type=\"submit\"",
				" value=\"ok\"",
				">");

			htmlParagraphClose (
				formatWriter);

			htmlFormClose (
				formatWriter);

			if (! ready)
				return;

			htmlParagraphOpen (
				formatWriter,
				htmlClassAttribute (
					"links"));

			switch (viewMode) {

			case daily:

				myUrlParams.set (
					"view",
					"daily");

				myUrlParams.set (
					"date",
					timeFormatter.dateStringShort (
						dateField.date.minusWeeks (1)));

				htmlLinkWrite (
					formatWriter,
					myUrlParams.toUrl (url),
					"Prev week");

				myUrlParams.set (
					"date",
					timeFormatter.dateStringShort (
						dateField.date.plusWeeks (1)));

				htmlLinkWrite (
					formatWriter,
					myUrlParams.toUrl (url),
					"Next week");

				break;

			case weekly:

				myUrlParams.set (
					"view",
					"weekly");

				myUrlParams.set (
					"date",
					timeFormatter.dateStringShort (
						dateField.date.minusDays (49)));

				htmlLinkWrite (
					formatWriter,
					myUrlParams.toUrl (url),
					"Prev weeks");

				myUrlParams.set (
					"date",
					timeFormatter.dateStringShort (
						dateField.date.plusDays (49)));

				htmlLinkWrite (
					formatWriter,
					myUrlParams.toUrl (url),
					"Next weeks");

				break;

			case monthly:

				myUrlParams.set (
					"view",
					"monthly");

				myUrlParams.set (
					"date",
					timeFormatter.dateStringShort (
						dateField.date.minusMonths (6)));

				htmlLinkWrite (
					formatWriter,
					myUrlParams.toUrl (url),
					"Prev months");

				myUrlParams.set (
					"date",
					timeFormatter.dateStringShort (
						dateField.date.plusMonths (6)));

				htmlLinkWrite (
					formatWriter,
					myUrlParams.toUrl (url),
					"Next months");

				break;
			}

			htmlParagraphClose (
				formatWriter);

			UrlParams groupedUrlParams =
				new UrlParams (urlParams);

			groupedUrlParams.remove ("split");

			GroupedStatsSource groupedStatsSource =
				groupedStatsSourceProvider.provide (
					transaction)

				.groupCriteria (
					splitCriteria)

				.statsSource (
					statsSource)

				.critMap (
					criteriaMap)

				.filterMap (
					statsConsoleLogic.makeFilterMap (
						transaction))

				.url (
					url)

				.urlParams (
					groupedUrlParams);

			switch (viewMode) {

			case daily:

				statsFormatterProvider.provide (
					transaction)

					.groupedStatsSource (
						groupedStatsSource)

					.mainDate (
						dateField.date)

					.timeScheme (
						smsStatsDailyTimeScheme)

					.go (
						transaction,
						formatWriter);

				break;

			case weekly:

				statsFormatterProvider.provide (
					transaction)

					.groupedStatsSource (
						groupedStatsSource)

					.mainDate (
						dateField.date)

					.timeScheme (
						smsStatsWeeklyTimeScheme)

					.go (
						transaction,
						formatWriter);

				break;

			case monthly:

				statsFormatterProvider.provide (
					transaction)

					.groupedStatsSource (
						groupedStatsSource)

					.mainDate (
						dateField.date)

					.timeScheme (
						smsStatsMonthlyTimeScheme)

					.go (
						transaction,
						formatWriter);

				break;

			}

		}

	}

	class ViewTab
		extends Tab {

		String viewParam;

		ViewTab (
				String label,
				String newViewParam) {

			super (label);

			viewParam = newViewParam;

		}

		@Override
		public
		String getUrl (
				@NonNull Transaction parentTransaction) {

			try (

				NestedTransaction transaction =
					parentTransaction.nestTransaction (
						logContext,
						"getUrl");

			) {

				UrlParams myUrlParams =
					new UrlParams (urlParams);

				myUrlParams.set (
					"date",
					dateField.text);

				myUrlParams.set (
					"view",
					viewParam);

				return myUrlParams.toUrl (url);

			}

		}

	};

	class TotalTab
		extends Tab {

		TotalTab () {

			super (
				"Total");

		}

		@Override
		public
		String getUrl (
				@NonNull Transaction parentTransaction) {

			try (

				NestedTransaction transaction =
					parentTransaction.nestTransaction (
						logContext,
						"getUrl");

			) {

				UrlParams urlParams =
					new UrlParams ();

				for (
					Map.Entry <SmsStatsCriteria, Set <Long>> entry
						: criteriaMap.entrySet ()
				) {

					SmsStatsCriteria crit =
						entry.getKey ();

					Long critId =
						entry.getValue ().iterator ().next ();

					urlParams.add (
						crit.toString (),
						Long.toString (
							critId));

				}

				urlParams.add (
					"view",
					optionalOrNull (
						requestContext.parameter (
							"view")));

				urlParams.add (
					"date",
					optionalOrNull (
						requestContext.parameter (
							"date")));

				return urlParams.toUrl (
					url);

			}

		}

	}

	class StatsTab
		extends Tab {

		SmsStatsCriteria myCriteria;

		StatsTab (
				SmsStatsCriteria newMyCriteria) {

			super (
				newMyCriteria.toString ());

			myCriteria =
				newMyCriteria;

		}

		@Override
		public
		String getUrl (
				@NonNull Transaction parentTransaction) {

			try (

				NestedTransaction transaction =
					parentTransaction.nestTransaction (
						logContext,
						"getUrl");

			) {

				UrlParams urlParams =
					new UrlParams ();

				urlParams.add (
					"split",
					myCriteria.toString ());

				for (
					Map.Entry <SmsStatsCriteria, Set <Long>> entry
						: criteriaMap.entrySet ()
				) {

					SmsStatsCriteria crit =
						entry.getKey ();

					Long critId =
						entry.getValue ().iterator ().next ();

					if (crit == myCriteria)
						continue;

					urlParams.add (
						crit.toString (),
						Long.toString (
							critId));

				}

				urlParams.add (
					"view",
					optionalOrNull (
						requestContext.parameter (
							"view")));

				urlParams.add (
					"date",
					optionalOrNull (
						requestContext.parameter (
							"date")));

				return urlParams.toUrl (url);

			}

		}

	}

}
