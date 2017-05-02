package wbs.sms.modempoll.model;

import org.joda.time.Instant;

import wbs.framework.database.Transaction;

public
interface ModemPollQueueDaoMethods {

	ModemPollQueueRec findNext (
			Transaction parentTransaction,
			Instant now);

}