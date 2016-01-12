package wbs.sms.message.stats.console;

import static wbs.framework.utils.etc.Misc.equal;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.LocalDate;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import wbs.framework.application.annotations.PrototypeComponent;
import wbs.sms.message.stats.model.MessageStatsObjectHelper;
import wbs.sms.message.stats.model.MessageStatsRec;
import wbs.sms.message.stats.model.MessageStatsSearch;
import wbs.sms.route.core.console.RouteConsoleHelper;
import wbs.sms.route.core.model.RouteRec;

@Accessors (fluent = true)
@PrototypeComponent ("smsStatsSource")
public
class SmsStatsSourceImplementation
	implements SmsStatsSource {

	// dependencies

	@Inject
	MessageStatsObjectHelper messageStatsHelper;

	@Inject
	RouteConsoleHelper routeHelper;

	@Inject
	SmsStatsConsoleLogic smsStatsConsoleLogic;

	// state

	@Getter @Setter
	Map<SmsStatsCriteria,Set<Integer>> fixedCriteriaMap =
		Collections.emptyMap ();

	// implementation

	@Override
	public
	List<MessageStatsRec> findMessageStats (
			@NonNull LocalDate startDate,
			@NonNull LocalDate endDate,
			@NonNull SmsStatsTimeScheme timeScheme,
			@NonNull Optional<SmsStatsCriteria> groupCriteria,
			@NonNull Map<SmsStatsCriteria,Set<Integer>> dynamicCriteriaMap,
			@NonNull Optional<Map<SmsStatsCriteria,Set<Integer>>> filterMap) {

		Map<SmsStatsCriteria,Set<Integer>> intersectedCriteriaMap =
			smsStatsConsoleLogic.criteriaMapIntersect (
				ImmutableList.<Map<SmsStatsCriteria,Set<Integer>>>of (
					fixedCriteriaMap,
					dynamicCriteriaMap));

		MessageStatsSearch search =
			smsStatsConsoleLogic.critMapToMessageStatsSearch (
				intersectedCriteriaMap,
				filterMap);

		search

			.dateAfter (
				startDate)

			.dateBefore (
				endDate)

			.group (
				true)

			.groupByDate (
				timeScheme.groupByDate ())

			.groupByMonth (
				timeScheme.groupByMonth ())

			.groupByAffiliate (
				equal (
					groupCriteria,
					Optional.of (
						SmsStatsCriteria.affiliate)))

			.groupByBatch (
				equal (
					groupCriteria,
					Optional.of (
						SmsStatsCriteria.batch)))

			.groupByNetwork (
				equal (
					groupCriteria,
					Optional.of (
						SmsStatsCriteria.network)))

			.groupByRoute (
				equal (
					groupCriteria,
					Optional.of (
						SmsStatsCriteria.route)))

			.groupByService (
				equal (
					groupCriteria,
					Optional.of (
						SmsStatsCriteria.service)));

		return messageStatsHelper.search (
			search);

	}

	@Override
	public
	RouteRec findRoute () {

		Collection<Integer> routeIds =
			fixedCriteriaMap.get (
				SmsStatsCriteria.route);

		if (routeIds == null
				|| routeIds.size () != 1)
			return null;

		return routeHelper.find (
			routeIds.iterator ().next ());

	}

}
