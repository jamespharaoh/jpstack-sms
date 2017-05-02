package wbs.sms.message.delivery.model;

import java.util.List;

import wbs.framework.database.Transaction;

public
interface DeliveryDaoMethods {

	List <DeliveryRec> findAllLimit (
			Transaction parentTransaction,
			Long maxResults);

}