package wbs.sms.message.delivery.model;

import java.util.List;

public
interface DeliveryDaoMethods {

	List<DeliveryRec> findAllLimit (
			int maxResults);

}