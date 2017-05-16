package wbs.sms.message.stats.console;

import static wbs.utils.collection.CollectionUtils.collectionDoesNotHaveOneElement;
import static wbs.utils.collection.CollectionUtils.iterableFirstElementRequired;
import static wbs.utils.etc.OptionalUtils.optionalAbsent;
import static wbs.utils.etc.OptionalUtils.optionalOf;
import static wbs.utils.etc.OptionalUtils.optionalValueEqualSafe;
import static wbs.utils.etc.NullUtils.isNull;

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

import wbs.framework.component.annotations.ClassSingletonDependency;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.component.annotations.SingletonDependency;
import wbs.framework.database.NestedTransaction;
import wbs.framework.database.Transaction;
import wbs.framework.logging.LogContext;

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

	@ClassSingletonDependency
	LogContext logContext;

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
	List <MessageStatsRec> findMessageStats (
			@NonNull Transaction parentTransaction,
			@NonNull LocalDate startDate,
			@NonNull LocalDate endDate,
			@NonNull SmsStatsTimeScheme timeScheme,
			@NonNull Optional <SmsStatsCriteria> groupCriteria,
			@NonNull Map <SmsStatsCriteria, Set <Long>> dynamicCriteriaMap,
			@NonNull Optional <Map <SmsStatsCriteria, Set <Long>>> filterMap) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findMessageStats");

		) {

			Map <SmsStatsCriteria, Set <Long>> intersectedCriteriaMap =
				smsStatsConsoleLogic.criteriaMapIntersect (
					transaction,
					ImmutableList.of (
						fixedCriteriaMap,
						dynamicCriteriaMap));

			MessageStatsSearch search =
				smsStatsConsoleLogic.critMapToMessageStatsSearch (
					transaction,
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
				transaction,
				search);

		}

	}

	@Override
	public
	Optional <RouteRec> findRoute (
			@NonNull Transaction parentTransaction) {

		try (

			NestedTransaction transaction =
				parentTransaction.nestTransaction (
					logContext,
					"findRoute");

		) {

			Collection <Long> routeIds =
				fixedCriteriaMap.get (
					SmsStatsCriteria.route);

			if (

				isNull (
					routeIds)

				|| collectionDoesNotHaveOneElement (
					routeIds)

			) {

				return optionalAbsent ();

			} else {

				return optionalOf (
					routeHelper.findRequired (
						transaction,
						iterableFirstElementRequired (
							routeIds)));

			}

		}

	}

}
