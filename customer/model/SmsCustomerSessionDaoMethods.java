package wbs.sms.customer.model;

import java.util.List;

import org.joda.time.Instant;

import wbs.framework.database.Transaction;

public
interface SmsCustomerSessionDaoMethods {

	List <SmsCustomerSessionRec> findToTimeoutLimit (
			Transaction parentTransaction,
			SmsCustomerManagerRec manager,
			Instant startedBefore,
			Long batchSize);

}