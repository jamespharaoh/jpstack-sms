package wbs.sms.message.stats.console;

import static wbs.framework.utils.etc.Misc.toEnum;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.inject.Inject;
import javax.inject.Provider;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.html.ObsoleteDateField;
import wbs.console.misc.TimeFormatter;
import wbs.console.part.AbstractPagePart;
import wbs.console.tab.Tab;
import wbs.console.tab.TabList;
import wbs.framework.application.annotations.PrototypeComponent;
import wbs.framework.web.UrlParams;

@Accessors (fluent = true)
@PrototypeComponent ("genericMessageStatsPart")
public
class GenericMessageStatsPart
	extends AbstractPagePart {

	// dependencies

	@Inject
	SmsStatsDailyTimeScheme smsStatsDailyTimeScheme;

	@Inject
	SmsStatsMonthlyTimeScheme smsStatsMonthlyTimeScheme;

	@Inject
	SmsStatsWeeklyTimeScheme smsStatsWeeklyTimeScheme;

	@Inject
	SmsStatsConsoleLogic statsConsoleLogic;

	@Inject
	TimeFormatter timeFormatter;

	// prototype dependencies

	@Inject
	Provider<GroupedStatsSource> groupedStatsSourceProvider;

	@Inject
	Provider<SmsStatsFormatter> statsFormatterProvider;

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

	TabList viewTabs =
		new TabList ();

	TabList.Prepared viewTabsPrepared;

	TabList splitTabs =
		new TabList ();

	TabList.Prepared splitTabsPrepared;

	Map<SmsStatsCriteria,Set<Integer>> criteriaMap =
		new HashMap<SmsStatsCriteria,Set<Integer>> ();

	Map<String,String> criteriaInfo =
		new TreeMap<String,String> ();

	SmsStatsCriteria splitCriteria;

	UrlParams urlParams =
		new UrlParams ();

	boolean ready;

	ObsoleteDateField dateField;

	// implementation

	public
	void prepareParams () {

		// check split param

		splitCriteria =
			toEnum (
				SmsStatsCriteria.class,
				requestContext.parameter ("split"));

		if (splitCriteria != null)
			urlParams.add (
				"split",
				splitCriteria.toString ());

		// check stats params

		for (SmsStatsCriteria crit
				: SmsStatsCriteria.values ()) {

			String param =
				requestContext.parameter (crit.toString ());

			if (param == null)
				continue;

			int critId =
				Integer.parseInt (param);

			criteriaMap.put (
				crit,
				Collections.singleton (critId));

			criteriaInfo.put (
				crit.toString (),
				statsConsoleLogic.lookupGroupName (crit, critId));

			urlParams.set (
				crit.toString (),
				critId);

		}

		// check view param

		viewMode =
			toEnum (
				SmsStatsViewMode.class,
				requestContext.parameter ("view"));

		if (viewMode == null)
			viewMode = SmsStatsViewMode.daily;

		urlParams.set (
			"view",
			viewMode.toString ());

		// check date param

		dateField =
			ObsoleteDateField.parse (
				requestContext.parameter ("date"));

		urlParams.set (
			"date",
			dateField.text);

	}

	public
	void prepareTabs () {

		// prepare split tabs

		Tab splitTab;

		splitTabs.add (
			splitTab = new TotalTab ());

		for (SmsStatsCriteria criteria
				: SmsStatsCriteria.values ()) {

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
				splitTab);

		// prepare view tabs

		Tab viewTab = null;

		for (SmsStatsViewMode smsStatsViewMode
				: SmsStatsViewMode.values ()) {

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
				viewTab);

	}

	@Override
	public
	void prepare () {

		// process page params

		prepareParams ();

		// prepre tabs

		prepareTabs ();

		// check inputs

		if (dateField.date == null) {

			requestContext.addError (
				"Invalid date format");

			return;

		}

		ready = true;

	}

	@Override
	public
	void renderHtmlHeadContent () {

		super.renderHtmlHeadContent ();

		printFormat (
			"<style type=\"text/css\">\n",
			"table.list td.group-name { background-color: #808080; color: white; font-weight: bold; }\n",
			"table.list td.group-name-hover { background-color: #404040; color: white; font-weight: bold; }\n",
			"table.list td.plain { background-color: #eeeeee; }\n",
			"table.list td.unknown { background-color: #ffffc0; }\n",
			"table.list td.succeeded { background-color: #c0ffc0; }\n",
			"table.list td.failed { background-color: #ffc0c0; }\n",
			"table.list td.hi-plain { background-color: #cecece; }\n",
			"table.list td.hi-unknown { background-color: #e0e0a0; }\n",
			"table.list td.hi-succeeded { background-color: #a0e0a0; }\n",
			"table.list td.hi-failed { background-color: #e0a0a0; }\n",
			"table.list td.revenue { background-color: #3366CC; color: white;}\n",
			"table.list td.hi-revenue { background-color: #6666CC; color: white;}\n",
			"</style>\n");

	}

	@Override
	public
	void renderHtmlBodyContent () {

		splitTabsPrepared.go (
			requestContext);

		viewTabsPrepared.go (
			requestContext);

		if (criteriaInfo.size () > 0) {

			printFormat (
				"<table class=\"details\">\n");

			for (Map.Entry<String,String> entry
					: criteriaInfo.entrySet ()) {

				String name =
					entry.getKey ();

				String value =
					entry.getValue ();

				printFormat (
					"<tr>\n",

					"<th>%h</th>\n",
					name,

					"<td>%h</td>\n",
					value,

					"</tr>\n");

			}

			printFormat (
				"</table>\n");

		}

		printFormat (
			"<form\n",
			" method=\"get\"",
			" action=\"%h\"",
			url,
			"\">\n");

		UrlParams myUrlParams =
			new UrlParams (
				urlParams);

		urlParams.remove (
			"date");

		urlParams.printHidden (
			printWriter);

		printFormat (
			"<p>Date<br>\n",

			"<input",
			" type=\"text\"",
			" name=\"date\"",
			" value=\"%h\"",
			dateField.text,
			">\n",

			"<input",
			" type=\"submit\"",
			" value=\"ok\"",
			"></p>\n");

		printFormat (
			"</form>");

		if (! ready)
			return;

		printFormat (
			"<p class=\"links\">\n");

		switch (viewMode) {

		case daily:

			myUrlParams.set (
				"view",
				"daily");

			myUrlParams.set (
				"date",
				timeFormatter.localDateToDateString (
					dateField.date.minusWeeks (1)));

			printFormat (
				"<a href=\"%h\">Prev week</a>\n",
				myUrlParams.toUrl (url));

			myUrlParams.set (
				"date",
				timeFormatter.localDateToDateString (
					dateField.date.plusWeeks (1)));

			printFormat (
				"<a href=\"%h\">Next week</a>\n",
				myUrlParams.toUrl (url));

			break;

		case weekly:

			myUrlParams.set (
				"view",
				"weekly");

			myUrlParams.set (
				"date",
				timeFormatter.localDateToDateString (
					dateField.date.minusDays (49)));

			printFormat (
				"<a href=\"%h\">Prev weeks</a>\n",
				myUrlParams.toUrl (url));

			myUrlParams.set (
				"date",
				timeFormatter.localDateToDateString (
					dateField.date.plusDays (98)));

			printFormat (
				"<a href=\"%h\">Next weeks</a>\n",
				myUrlParams.toUrl (url));

			break;

		case monthly:

			myUrlParams.set (
				"view",
				"monthly");

			myUrlParams.set (
				"date",
				timeFormatter.localDateToDateString (
					dateField.date.minusMonths (6)));

			printFormat (
				"<a href=\"%h\">Prev months</a>\n",
				myUrlParams.toUrl (url));

			myUrlParams.set (
				"date",
				timeFormatter.localDateToDateString (
					dateField.date.plusMonths (12)));

			printFormat (
				"<a href=\"%h\">Next months</a>",
				myUrlParams.toUrl (url));

			break;
		}

		printFormat (
			"</p>\n");

		UrlParams groupedUrlParams =
			new UrlParams (urlParams);

		groupedUrlParams.remove ("split");

		GroupedStatsSource groupedStatsSource =
			groupedStatsSourceProvider.get ()

			.groupCriteria (
				splitCriteria)

			.statsSource (
				statsSource)

			.critMap (
				criteriaMap)

			.filterMap (
				statsConsoleLogic.makeFilterMap ())

			.url (
				url)

			.urlParams (
				groupedUrlParams);

		switch (viewMode) {

		case daily:

			statsFormatterProvider.get ()

				.groupedStatsSource (
					groupedStatsSource)

				.mainDate (
					dateField.date)

				.timeScheme (
					smsStatsDailyTimeScheme)

				.go ();

			break;

		case weekly:

			statsFormatterProvider.get ()

				.groupedStatsSource (
					groupedStatsSource)

				.mainDate (
					dateField.date)

				.timeScheme (
					smsStatsWeeklyTimeScheme)

				.go ();

			break;

		case monthly:

			statsFormatterProvider.get ()

				.groupedStatsSource (
					groupedStatsSource)

				.mainDate (
					dateField.date)

				.timeScheme (
					smsStatsMonthlyTimeScheme)

				.go ();

			break;

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
		String getUrl () {

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

	};

	class TotalTab
		extends Tab {

		TotalTab () {

			super (
				"Total");

		}

		@Override
		public
		String getUrl () {

			UrlParams urlParams =
				new UrlParams ();

			for (Map.Entry<SmsStatsCriteria,Set<Integer>> entry
					: criteriaMap.entrySet ()) {

				SmsStatsCriteria crit =
					entry.getKey ();

				int critId =
					entry.getValue ().iterator ().next ();

				urlParams.add (
					crit.toString (),
					Integer.toString (critId));

			}

			urlParams.add (
				"view",
				requestContext.parameter ("view"));

			urlParams.add (
				"date",
				requestContext.parameter ("date"));

			return urlParams.toUrl (
				url);

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
		String getUrl () {

			UrlParams urlParams =
				new UrlParams ();

			urlParams.add (
				"split",
				myCriteria.toString ());

			for (Map.Entry<SmsStatsCriteria,Set<Integer>> entry
					: criteriaMap.entrySet ()) {

				SmsStatsCriteria crit =
					entry.getKey ();

				int critId =
					entry.getValue ().iterator ().next ();

				if (crit == myCriteria)
					continue;

				urlParams.add (
					crit.toString (),
					Integer.toString (critId));

			}

			urlParams.add (
				"view",
				requestContext.parameter ("view"));

			urlParams.add (
				"date",
				requestContext.parameter ("date"));

			return urlParams.toUrl (url);

		}

	}

}
