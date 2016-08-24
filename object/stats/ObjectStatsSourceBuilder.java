package wbs.sms.object.stats;

import wbs.framework.entity.record.Record;
import wbs.sms.message.stats.console.SmsStatsSource;

public
interface ObjectStatsSourceBuilder {

	SmsStatsSource buildStatsSource (
		Record<?> parent);

}
