package wbs.sms.message.stats.console;

import java.util.Map;
import java.util.Set;

public
interface SmsStatsConsoleLogic {

	Map<SmsStatsCriteria,Set<Integer>> makeFilterMap ();

	String lookupGroupName (
			SmsStatsCriteria crit,
			int id);

	Map<SmsStatsCriteria,Set<Integer>> criteriaMapIntersect (
			Map<SmsStatsCriteria,
			Set<Integer>>... critMaps);

	String nameForCriteria (
			SmsStatsCriteria statsCriteria);

	Map<String,Object> critMapToMessageStatsEntrySearchMap (
			Map<SmsStatsCriteria, Set<Integer>> critMap,
			Map<SmsStatsCriteria, Set<Integer>> filterMap);

}
