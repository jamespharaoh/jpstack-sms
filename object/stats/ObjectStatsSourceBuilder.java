package wbs.sms.object.stats;

import wbs.framework.database.Transaction;
import wbs.framework.entity.record.Record;

import wbs.sms.message.stats.console.SmsStatsSource;

public
interface ObjectStatsSourceBuilder {

	SmsStatsSource buildStatsSource (
			Transaction parentTransaction,
			Record <?> parent);

}
