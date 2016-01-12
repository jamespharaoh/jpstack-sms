package wbs.sms.message.stats.console;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.LocalDate;

import com.google.common.base.Optional;

import wbs.sms.message.stats.model.MessageStatsRec;
import wbs.sms.route.core.model.RouteRec;

/**
 * Instances of this interface will find a certain set of stats entries,
 * aggregated by some criteria, and mapped by date. It then returns them in a
 * map of maps.
 */
public
interface SmsStatsSource {

	/**
	 * Finds all message stats entries for the given time period, filtering to
	 * the entries given by the map.
	 */
	List<MessageStatsRec> findMessageStats (
			LocalDate startDate,
			LocalDate endDate,
			Optional<SmsStatsCriteria> groupCriteria,
			Map<SmsStatsCriteria,Set<Integer>> criteriaMap,
			Optional<Map<SmsStatsCriteria,Set<Integer>>> filterMap);

	/**
	 * Finds the default route for this source. This returns null if there is no
	 * fixed route.
	 */
	RouteRec findRoute ();

}
