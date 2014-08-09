package wbs.sms.object.stats;

import wbs.framework.record.Record;
import wbs.sms.message.stats.console.SmsStatsSource;

public
interface ObjectStatsSourceBuilder {

	SmsStatsSource buildStatsSource (
		Record<?> parent);

}
