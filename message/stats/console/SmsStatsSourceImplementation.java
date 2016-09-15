package wbs.sms.message.stats.console;

import static wbs.utils.collection.CollectionUtils.collectionDoesNotHaveOneElement;
import static wbs.utils.collection.CollectionUtils.iterableFirstElementRequired;
import static wbs.utils.etc.Misc.isNull;
import static wbs.utils.etc.OptionalUtils.optionalValueEqualSafe;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.joda.time.LocalDate;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
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

	// singleton dependencies

	@SingletonDependency
	MessageStatsObjectHelper messageStatsHelper;

	@SingletonDependency
	RouteConsoleHelper routeHelper;

	@SingletonDependency
	SmsStatsConsoleLogic smsStatsConsoleLogic;

	// state

	@Getter @Setter
	Map <SmsStatsCriteria, Set <Long>> fixedCriteriaMap =
		Collections.emptyMap ();

	// implementation

	@Override
	public
	List<MessageStatsRec> findMessageStats (
			@NonNull LocalDate startDate,
			@NonNull LocalDate endDate,
			@NonNull SmsStatsTimeScheme timeScheme,
			@NonNull Optional<SmsStatsCriteria> groupCriteria,
			@NonNull Map<SmsStatsCriteria,Set<Long>> dynamicCriteriaMap,
			@NonNull Optional<Map<SmsStatsCriteria,Set<Long>>> filterMap) {

		Map<SmsStatsCriteria,Set<Long>> intersectedCriteriaMap =
			smsStatsConsoleLogic.criteriaMapIntersect (
				ImmutableList.of (
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
				optionalValueEqualSafe (
					groupCriteria,
					SmsStatsCriteria.affiliate))

			.groupByBatch (
				optionalValueEqualSafe (
					groupCriteria,
					SmsStatsCriteria.batch))

			.groupByNetwork (
				optionalValueEqualSafe (
					groupCriteria,
					SmsStatsCriteria.network))

			.groupByRoute (
				optionalValueEqualSafe (
					groupCriteria,
					SmsStatsCriteria.route))

			.groupByService (
				optionalValueEqualSafe (
					groupCriteria,
					SmsStatsCriteria.service));

		return messageStatsHelper.search (
			search);

	}

	@Override
	public
	RouteRec findRoute () {

		Collection <Long> routeIds =
			fixedCriteriaMap.get (
				SmsStatsCriteria.route);

		if (

			isNull (
				routeIds)

			|| collectionDoesNotHaveOneElement (
				routeIds)

		) {
			return null;
		}

		return routeHelper.findRequired (
			iterableFirstElementRequired (
				routeIds));

	}

}
