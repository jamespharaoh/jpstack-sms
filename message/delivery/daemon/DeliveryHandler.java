package wbs.sms.message.delivery.daemon;

import java.util.Collection;

import wbs.framework.logging.TaskLogger;

public
interface DeliveryHandler {

	Collection <String> getDeliveryTypeCodes ();

	void handle (
			TaskLogger parentTaskLogger,
			Long deliveryNoticeQueueId,
			Long ref);

}
