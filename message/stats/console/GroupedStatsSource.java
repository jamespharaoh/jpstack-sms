package wbs.sms.message.stats.console;

import java.util.HashMap;
import java.util.Map;

import org.joda.time.LocalDate;

import wbs.sms.message.stats.model.MessageStatsData;
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
		Map<LocalDate,MessageStatsData> statsByDate;

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
				new HashMap<LocalDate,MessageStatsData> ();

		}

		public
		GroupStats (
				RouteRec newRoute) {

			route = newRoute;
			url = null;

			statsByDate =
				new HashMap<LocalDate,MessageStatsData> ();

		}

		public
		GroupStats (
				Map<LocalDate,MessageStatsData> newStatsByDate,
				RouteRec newRoute,
				String newUrl) {

			statsByDate = newStatsByDate;
			route = newRoute;
			url = newUrl;

		}

		public
		Map<LocalDate,MessageStatsData> getStatsByDate () {

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
