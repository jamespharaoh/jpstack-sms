package wbs.sms.customer.model;

import java.util.List;

import org.joda.time.Instant;

public
interface SmsCustomerSessionDaoMethods {

	List <SmsCustomerSessionRec> findToTimeoutLimit (
			SmsCustomerManagerRec manager,
			Instant startedBefore,
			Long batchSize);

}