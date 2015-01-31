package wbs.sms.message.stats.console;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.LocalDate;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.sms.message.stats.model.MessageStatsObjectHelper;
import wbs.sms.message.stats.model.MessageStatsRec;
import wbs.sms.message.stats.model.MessageStatsRec.MessageStatsSearch;
import wbs.sms.route.core.console.RouteConsoleHelper;
import wbs.sms.route.core.model.RouteRec;

@Accessors (fluent = true)
@PrototypeComponent ("smsStatsSourceImpl")
public
class SmsStatsSourceImpl
	implements SmsStatsSource {

	@Inject
	MessageStatsObjectHelper messageStatsHelper;

	@Inject
	RouteConsoleHelper routeHelper;

	@Inject
	SmsStatsConsoleLogic smsStatsConsoleLogic;

	@Getter @Setter
	Map<SmsStatsCriteria,Set<Integer>> fixedCriteriaMap =
		Collections.emptyMap ();

	@Override
	public
	List<MessageStatsRec> findMessageStats (
			LocalDate startDate,
			LocalDate endDate,
			Map<SmsStatsCriteria,Set<Integer>> dynamicCriteriaMap,
			Map<SmsStatsCriteria,Set<Integer>> filterMap) {

		@SuppressWarnings ("unchecked")
		Map<SmsStatsCriteria,Set<Integer>> intersectedCriteriaMap =
			smsStatsConsoleLogic.criteriaMapIntersect (
				fixedCriteriaMap,
				dynamicCriteriaMap);

		MessageStatsSearch search =
			smsStatsConsoleLogic.critMapToMessageStatsSearch (
				intersectedCriteriaMap,
				filterMap);

		search

			.dateAfter (
				startDate)

			.dateBefore (
				endDate);

		return messageStatsHelper.search (
			search);

	}

	@Override
	public
	RouteRec findRoute () {

		Collection<Integer> routeIds =
			fixedCriteriaMap.get (SmsStatsCriteria.route);

		if (routeIds == null
				|| routeIds.size () != 1)
			return null;

		return routeHelper.find (
			routeIds.iterator ().next ());

	}

}
