package wbs.sms.message.stats.console;

import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.Misc.toEnum;
import static wbs.utils.etc.OptionalUtils.optionalIsNotPresent;
import static wbs.utils.etc.OptionalUtils.optionalOrNull;
import static wbs.utils.web.HtmlAttributeUtils.htmlClassAttribute;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphClose;
import static wbs.utils.web.HtmlBlockUtils.htmlParagraphOpen;
import static wbs.utils.web.HtmlFormUtils.htmlFormClose;
import static wbs.utils.web.HtmlFormUtils.htmlFormOpenGetAction;
import static wbs.utils.web.HtmlTableUtils.htmlTableClose;
import static wbs.utils.web.HtmlTableUtils.htmlTableDetailsRowWrite;
import static wbs.utils.web.HtmlTableUtils.htmlTableOpenDetails;
import static wbs.utils.web.HtmlUtils.htmlLinkWrite;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.inject.Provider;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import wbs.console.html.HtmlLink;
import wbs.console.html.ObsoleteDateField;
import wbs.console.part.AbstractPagePart;
import wbs.console.tab.Tab;
import wbs.console.tab.TabList;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.PrototypeDependency;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.web.UrlParams;
import wbs.utils.time.TimeFormatter;

@Accessors (fluent = true)
@PrototypeComponent ("genericMessageStatsPart")
public
class GenericMessageStatsPart
	extends AbstractPagePart {

	// singleton dependencies

	@SingletonDependency
	SmsStatsDailyTimeScheme smsStatsDailyTimeScheme;

	@SingletonDependency
	SmsStatsMonthlyTimeScheme smsStatsMonthlyTimeScheme;

	@SingletonDependency
	SmsStatsWeeklyTimeScheme smsStatsWeeklyTimeScheme;

	@SingletonDependency
	SmsStatsConsoleLogic statsConsoleLogic;

	@SingletonDependency
	TimeFormatter timeFormatter;

	// prototype dependencies

	@PrototypeDependency
	Provider <GroupedStatsSource> groupedStatsSourceProvider;

	@PrototypeDependency
	Provider <SmsStatsFormatter> statsFormatterProvider;

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
	void prepareParams () {

		// check split param

		splitCriteria =
			toEnum (
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
					crit,
					critId));

			urlParams.set (
				crit.toString (),
				critId);

		}

		// check view param

		viewMode =
			toEnum (
				SmsStatsViewMode.class,
				requestContext.parameterOrEmptyString (
					"view"));

		if (viewMode == null)
			viewMode = SmsStatsViewMode.daily;

		urlParams.set (
			"view",
			viewMode.toString ());

		// check date param

		dateField =
			ObsoleteDateField.parse (
				requestContext.parameterOrNull (
					"date"));

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
				splitTab);

		// prepare view tabs

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

	@Override
	public
	void renderHtmlHeadContent () {

		super.renderHtmlHeadContent ();

	}

	@Override
	public
	void renderHtmlBodyContent () {

		splitTabsPrepared.go (
			formatWriter);

		viewTabsPrepared.go (
			formatWriter);

		if (criteriaInfo.size () > 0) {

			htmlTableOpenDetails ();

			for (
				Map.Entry <String, String> entry
					: criteriaInfo.entrySet ()
			) {

				String name =
					entry.getKey ();

				String value =
					entry.getValue ();

				htmlTableDetailsRowWrite (
					name,
					value);

			}

			htmlTableClose ();

		}

		htmlFormOpenGetAction (
			url);

		UrlParams myUrlParams =
			new UrlParams (
				urlParams);

		urlParams.remove (
			"date");

		urlParams.printHidden (
			formatWriter);

		htmlParagraphOpen ();

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

		htmlParagraphClose ();

		htmlFormClose ();

		if (! ready)
			return;

		htmlParagraphOpen (
			htmlClassAttribute (
				"links"));

		switch (viewMode) {

		case daily:

			myUrlParams.set (
				"view",
				"daily");

			myUrlParams.set (
				"date",
				timeFormatter.dateString (
					dateField.date.minusWeeks (1)));

			htmlLinkWrite (
				myUrlParams.toUrl (url),
				"Prev week");

			myUrlParams.set (
				"date",
				timeFormatter.dateString (
					dateField.date.plusWeeks (1)));

			htmlLinkWrite (
				myUrlParams.toUrl (url),
				"Next week");

			break;

		case weekly:

			myUrlParams.set (
				"view",
				"weekly");

			myUrlParams.set (
				"date",
				timeFormatter.dateString (
					dateField.date.minusDays (49)));

			htmlLinkWrite (
				myUrlParams.toUrl (url),
				"Prev weeks");

			myUrlParams.set (
				"date",
				timeFormatter.dateString (
					dateField.date.plusDays (49)));

			htmlLinkWrite (
				myUrlParams.toUrl (url),
				"Next weeks");

			break;

		case monthly:

			myUrlParams.set (
				"view",
				"monthly");

			myUrlParams.set (
				"date",
				timeFormatter.dateString (
					dateField.date.minusMonths (6)));

			htmlLinkWrite (
				myUrlParams.toUrl (url),
				"Prev months");

			myUrlParams.set (
				"date",
				timeFormatter.dateString (
					dateField.date.plusMonths (6)));

			htmlLinkWrite (
				myUrlParams.toUrl (url),
				"Next months");

			break;
		}

		htmlParagraphClose ();

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
