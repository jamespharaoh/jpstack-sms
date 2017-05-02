package wbs.sms.message.stats.console;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;

import org.joda.time.LocalDate;

import wbs.framework.database.Transaction;

import wbs.sms.message.stats.model.MessageStatsRec;
import wbs.sms.route.core.model.RouteRec;

public
interface SmsStatsSource {

	List <MessageStatsRec> findMessageStats (
			Transaction parentTransaction,
			LocalDate startDate,
			LocalDate endDate,
			SmsStatsTimeScheme timeScheme,
			Optional <SmsStatsCriteria> groupCriteria,
			Map <SmsStatsCriteria, Set <Long>> criteriaMap,
			Optional <Map <SmsStatsCriteria, Set <Long>>> filterMap);

	Optional <RouteRec> findRoute (
			Transaction parentTransaction);

}
