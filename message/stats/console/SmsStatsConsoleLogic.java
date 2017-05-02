package wbs.sms.message.stats.console;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;

import wbs.framework.database.Transaction;

import wbs.sms.message.stats.model.MessageStatsSearch;

public
interface SmsStatsConsoleLogic {

	Map <SmsStatsCriteria, Set <Long>> makeFilterMap (
			Transaction parentTransaction);

	String lookupGroupName (
			Transaction parentTransaction,
			SmsStatsCriteria crit,
			Long id);

	Map <SmsStatsCriteria, Set <Long>> criteriaMapIntersect (
			Transaction parentTransaction,
			List <Map <SmsStatsCriteria, Set <Long>>> critMaps);

	MessageStatsSearch setSearchCriteria (
			Transaction parentTransaction,
			MessageStatsSearch messageStatsSearch,
			SmsStatsCriteria statsCriteria,
			Collection <Long> value);

	MessageStatsSearch critMapToMessageStatsSearch (
			Transaction parentTransaction,
			Map <SmsStatsCriteria, Set <Long>> critMap,
			Optional <Map <SmsStatsCriteria, Set <Long>>> filterMap);

}
