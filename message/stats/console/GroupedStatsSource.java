package wbs.sms.message.stats.console;

import static wbs.utils.etc.OptionalUtils.optionalAbsent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;

import lombok.NonNull;

import org.joda.time.LocalDate;

import wbs.sms.message.stats.model.MessageStatsData;
import wbs.sms.route.core.model.RouteRec;

import wbs.web.misc.UrlParams;

public
interface GroupedStatsSource {

	public
	Map<String,GroupStats> load (
			SmsStatsTimeScheme timeScheme,
			LocalDate start,
			LocalDate end);

	public
	class GroupStats {

		private final
		Map <LocalDate, MessageStatsData> statsByDate;

		private final
		Optional <RouteRec> route;

		private final
		Optional <String> url;

		public
		GroupStats (
				@NonNull Map <LocalDate, MessageStatsData> statsByDate,
				@NonNull Optional <RouteRec> route,
				@NonNull Optional <String> url) {

			this.statsByDate =
				statsByDate;

			this.route =
				route;

			this.url =
				url;

		}

		public
		GroupStats (
				@NonNull Optional <RouteRec> route,
				@NonNull Optional <String> url) {

			this (
				new HashMap<> (),
				route,
				url);

		}

		public
		GroupStats (
				@NonNull Optional <RouteRec> route) {

			this (
				new HashMap<> (),
				route,
				optionalAbsent ());

		}

		public
		Map <LocalDate, MessageStatsData> getStatsByDate () {

			return statsByDate;

		}

		public
		Optional <RouteRec> getRoute () {
			return route;
		}

		public
		Optional <String> getUrl () {
			return url;
		}

	}

	GroupedStatsSource groupCriteria (
			SmsStatsCriteria groupCriteria);

	GroupedStatsSource statsSource (
			SmsStatsSource statsSource);

	GroupedStatsSource critMap (
			Map<SmsStatsCriteria,Set<Long>> critMap);

	GroupedStatsSource filterMap (
			Map<SmsStatsCriteria,Set<Long>> filterMap);

	GroupedStatsSource url (
			String url);

	GroupedStatsSource urlParams (
			UrlParams urlParams);

}
