package wbs.sms.message.stats.console;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import wbs.sms.message.stats.model.MessageStatsRec.MessageStatsSearch;

public
interface SmsStatsConsoleLogic {

	Map<SmsStatsCriteria,Set<Integer>> makeFilterMap ();

	String lookupGroupName (
			SmsStatsCriteria crit,
			int id);

	Map<SmsStatsCriteria,Set<Integer>> criteriaMapIntersect (
			Map<SmsStatsCriteria,
			Set<Integer>>... critMaps);

	MessageStatsSearch setSearchCriteria (
			MessageStatsSearch messageStatsSearch,
			SmsStatsCriteria statsCriteria,
			Collection<Integer> value);

	MessageStatsSearch critMapToMessageStatsSearch (
			Map<SmsStatsCriteria,Set<Integer>> critMap,
			Map<SmsStatsCriteria,Set<Integer>> filterMap);

}
