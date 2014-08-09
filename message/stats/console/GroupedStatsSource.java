package wbs.sms.message.stats.console;

import java.util.HashMap;
import java.util.Map;

import org.joda.time.LocalDate;

import wbs.sms.message.stats.model.MessageStats;
import wbs.sms.route.core.model.RouteRec;

public
interface GroupedStatsSource {

	public
	Map<String,GroupStats> load (
			LocalDate start,
			LocalDate end);

	public
	class GroupStats {

		private final
		Map<LocalDate,MessageStats> statsByDate;

		private final
		RouteRec route;

		private final
		String url;

		public
		GroupStats (
				RouteRec newRoute,
				String newUrl) {

			route = newRoute;
			url = newUrl;

			statsByDate =
				new HashMap<LocalDate,MessageStats> ();

		}

		public
		GroupStats (
				RouteRec newRoute) {

			route = newRoute;
			url = null;

			statsByDate =
				new HashMap<LocalDate,MessageStats> ();

		}

		public GroupStats (
				Map<LocalDate,MessageStats> newStatsByDate,
				RouteRec newRoute,
				String newUrl) {

			statsByDate = newStatsByDate;
			route = newRoute;
			url = newUrl;

		}

		public
		Map<LocalDate,MessageStats> getStatsByDate () {

			return statsByDate;

		}

		public
		RouteRec getRoute () {
			return route;
		}

		public
		String getUrl () {
			return url;
		}

	}

}
