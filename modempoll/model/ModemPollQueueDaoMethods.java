package wbs.sms.modempoll.model;

import org.joda.time.Instant;

public
interface ModemPollQueueDaoMethods {

	ModemPollQueueRec findNext (
		Instant now);

}